package com.example.demo.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.demo.config.ServiceConfig;
import com.example.demo.model.License;
import com.example.demo.model.Organization;
import com.example.demo.repository.LicenseRepository;
import com.example.demo.service.client.DiscoveryClientMode;
import com.example.demo.service.client.FeignClientMode;
import com.example.demo.service.client.OrganizationRestTemplateClient;
import com.example.demo.service.client.RestTemplateClient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class LicenseService {
	
	@Autowired
	MessageSource messages;
	
	@Autowired
	private LicenseRepository licenseRepository;
	
	@Autowired
	ServiceConfig config;
	
	//CLIENTS
	@Autowired
	DiscoveryClientMode discoveryClient;
	
	@Autowired
	RestTemplateClient restTemplateClient;
	
	@Autowired
	FeignClientMode feignClient;
	
	@Autowired
	OrganizationRestTemplateClient organizationRestClient; // usa REDIS!
	
	private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

	public License getLicense(String licenseId, String organizationId){
		License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
		if (null == license) {
			throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null),licenseId, organizationId));	
		}
		return license.withComment(config.getProperty());
	}
	
	//METODO PARA DIFERENTES CLIENTES!
	public License getLicense(String licenseId, String organizationId, String clientType) throws TimeoutException{
		License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
		if (null == license) {
			throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null),licenseId, organizationId));	
		}

		Organization organization = retrieveOrganizationInfo(organizationId, clientType);
		if (null != organization) {
			license.setOrganizationName(organization.getName());
			license.setContactName(organization.getContactName());
			license.setContactEmail(organization.getContactEmail());
			license.setContactPhone(organization.getContactPhone());
		}

		return license.withComment(config.getProperty());
	}
	// método usado por el de arriba
	private Organization retrieveOrganizationInfo(String organizationId, String clientType) throws TimeoutException {
		Organization organization = null;
		
		
		
		switch (clientType) {
		
		case "feign":
			System.out.println("I am using the feign client");
			organization = feignClient.getOrganization(organizationId);
			break;
		case "rest":
			System.out.println("I am using the rest client");
			organization = restTemplateClient.getOrganization(organizationId);
			break;
		case "discovery":
			System.out.println("I am using the discovery client");
			organization = discoveryClient.getOrganization(organizationId);
			break;
		default:
			organization = organizationRestClient.getOrganization(organizationId); // REDIS!!
			break;
		}
		
		

		return organization;
	}
	
	

	public License createLicense(License license){
		license.setLicenseId(UUID.randomUUID().toString());
		licenseRepository.save(license);

		return license.withComment(config.getProperty());
	}

	public License updateLicense(License license){
		licenseRepository.save(license);

		return license.withComment(config.getProperty());
	}

	public String deleteLicense(String licenseId){
		String responseMessage = null;
		License license = new License();
		license.setLicenseId(licenseId);
		licenseRepository.delete(license);
		responseMessage = String.format(messages.getMessage("license.delete.message", null, null),licenseId);
		return responseMessage;

	}
	
	/**
	 * Resilience4j implementation
	 * 
	 * Orden de prioridad, si uso varios, y todos catchean cierta excepción, este es el orden.
	 * 
	 * Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function ) ))))
	 * 
	 * With the use of the @CircuitBreaker annotation, any time the getLicensesByOrganization() method 
	 * is called, the call is wrapped with a Resilience4j circuit breaker. The circuit breaker interrupts 
	 * any failed attempt to call the getLicensesByOrganization() method.
	 * This code example would be boring if the database was working correctly. Let’s simulate the 
	 * getLicensesByOrganization() method running into a slow or timed out database query. 
	 * 
	 * Para ver ejemplo pegandole a un microservicio ver DiscoveryClientMode!
	 * 
	 * Part of the beauty of the circuit breaker pattern is that because a “middleman” 
	 * is between the consumer of a remote resource and the resource itself, we have the 
	 * opportunity to intercept a service failure and choose an alternative course of action to take.
	 * In Resilience4j, this is known as a fallback strategy.
	 * The fallback method must reside in the same class as the original method that was 
	 * protected by @CircuitBreaker.
	 * 
	 * Ahora, cuando exista un timeout error, en lugar de tirar excepción, nos devuelve nuestra
	 * fallback. Incluso cuando esté cerrado (es decir, operativo normal)!
	 * 
	 * Veamos el BULKHEAD PATTERN
	 * 
	 * The bulkhead pattern segregates remote resource calls in their own thread pools so that 
	 * a single misbehaving service can be contained and not crash the container.
	 * Resilience4j provides two different implementations of the bulkhead pattern. You can use 
	 * these implementations to limit the number of concurrent executions:
	 * Semaphore bulkhead (default): Uses a semaphore isolation approach, limiting the number of 
	 * concurrent requests to the service. Once the limit is reached, it starts rejecting requests.
	 * Thread pool bulkhead: Uses a bounded queue and a fixed thread pool. This approach only 
	 * rejects a request when the pool and the queue are full.
	 * 
	 * RETRY PATTERN
	 * 
	 * Funciona ejecutando retries cuando no puede conectar, intenta por ejemplo 5 veces con un cierto
	 * tiempo entre cada una de ellas. Prueba, no conecta, intenta de nuevo, 5 veces, y luego envia fallback.
	 * Podemos probarlo lanzando siempre excepción y usando SOLO este patrón. Esto por el orden de prioridad.
	 * 
	 * RATE LIMITER PATTERN
	 * 
	 * The main difference between the bulkhead and the rate limiter pattern is that the bulkhead pattern 
	 * is in charge of limiting the number of concurrent calls (for example, it only allows X concurrent 
	 * calls at a time). With the rate limiter, we can limit the number of total calls in a given timeframe 
	 * (for example, allow X number of calls every Y seconds).
	 * 
	 * CONCLUSION:
	 * 
	 * Podemos usar varios al mismo tiempo. Bulkhead controla concurrencia, Rate Limiter controla concurrencia
	 * dentro de una franja de tiempo específica, Circuit Breaker controla fallas en el servicio consecutivas
	 * usando un grupo de intentos (12 por ejemplo) y segun la tasa que fijemos corta la conexión para evitar
	 * el overhead; y finalmente Retry que permite volver a intentar x cantidad de veces cada tt tiempo cuando
	 * fracasa una conexión. Son patrones que nos permiten controlar el funcionamiento de conexiones a db o 
	 * microservicios en diferentes situaciones, pueden combinarse o no, segun la necesidad.
	 * TODOS generan un aspecto, un proxy, ante la excepción que fijemos, ojo con eso, ADEMÁS de la situación
	 * particular que controlan. Generalmente es un TimeoutException.
	 */
	//
	//@RateLimiter(name="licenseService", fallbackMethod = "buildFallbackLicenseList")
	//@Retry(name="retryLicenseService",fallbackMethod="fallbackRetry")
	//@Bulkhead(name="bulkheadLicenseService",fallbackMethod="fallbackBulk")
	@CircuitBreaker(name="licenseService",fallbackMethod="buildFallbackLicenseList")
	public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
		
		//logger.debug("getLicensesByOrganization Correlation id: {}",
				//UserContextHolder.getContext().getCorrelationId());
		randomlyRunLong();
		return licenseRepository.findByOrganizationId(organizationId);
	}
	
	private void randomlyRunLong() throws TimeoutException { 
		Random rand = new Random();
		int randomNum = rand.nextInt((3 - 1) + 1) + 1;
		if (randomNum==3) sleep();
	}
	private void sleep() throws TimeoutException {
		try {
			System.out.println("Sleep");
			Thread.sleep(500);
			throw new TimeoutException();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}
	/**
	 * To create the fallback method in Resilience4j, we need to create a method that contains 
	 * the same signature as the originating function plus one extra parameter, which is the 
	 * target exception parameter. With the same signature, we can pass all the parameters from 
	 * the original method to the fallback method.
	 * 
	 * We could have our fallback method read this data from an alternative data source, but 
	 * for demonstration purposes, we’re going to construct a list that can be returned by our 
	 * original function call.
	 * 
	 * Be aware of the actions you take with your fallback functions. If you call out to another 
	 * distributed service in your fallback service, you may need to wrap the fallback with 
	 * a @CircuitBreaker. Remember, the same failure that you’re experiencing with your primary 
	 * course of action might also impact your secondary fallback option. Code defensively.
	 * 
	 * @param organizationId
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<License> buildFallbackLicenseList(String organizationId, Throwable t){
		List<License> fallbackList = new ArrayList<>();
		License license = new License();
		license.setLicenseId("0000000-00-00000");
		license.setOrganizationId(organizationId);
		license.setProductName("Sorry no licensing information currently available");
		fallbackList.add(license);
		return fallbackList;
	}
	@SuppressWarnings("unused")
	private List<License> fallbackBulk(String organizationId, Throwable t){
		List<License> fallbackList = new ArrayList<>();
		License license = new License();
		license.setLicenseId("0000000-11-16168");
		license.setOrganizationId(organizationId);
		license.setProductName("Sorry Bulk pattern working...");
		fallbackList.add(license);
		return fallbackList;
	}
	@SuppressWarnings("unused")
	private List<License> fallbackRetry(String organizationId, Throwable t){
		List<License> fallbackList = new ArrayList<>();
		License license = new License();
		license.setLicenseId("0000000-22-88888");
		license.setOrganizationId(organizationId);
		license.setProductName("Sorry Retry pattern working...");
		fallbackList.add(license);
		return fallbackList;
	}
	
}
