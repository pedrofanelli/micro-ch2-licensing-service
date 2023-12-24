package com.example.demo.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Organization;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * The Spring Discovery Client offers the lowest level of access to the Load Balancer and the 
 * services registered within it. Using the Discovery Client, you can query for all the services 
 * registered with the Spring Cloud Load Balancer client and their corresponding URLs.
 * 
 * You use this class to interact with the Spring Cloud Load Balancer.
 * 
 * Then, to retrieve all instances of the organization services registered with Eureka, 
 * you use the getInstances() method, passing in the service key that you’re looking for 
 * to retrieve a list of ServiceInstance objects. The ServiceInstance class holds 
 * information about a specific instance of a service, including its hostname, port, and URI.
 * 
 * There are some cons:
 * 1) You aren’t taking advantage of the Spring Cloud client-side Load Balancer. By calling the 
 * Discovery Client directly, you get a list of services, but it becomes your responsibility to 
 * choose which returned service instance you’re going to invoke.
 * 
 * 2) You’re doing too much work. In the code, you have to build the URL that you’ll use to call 
 * your service. It’s a small thing, but every piece of code that you can avoid writing is one 
 * less piece of code that you have to debug.
 * 
 * 3) Observant Spring developers might have also noticed that we directly instantiated the 
 * RestTemplate class in the code. This is antithetical to usual Spring REST invocations because 
 * you’ll usually have the Spring framework inject the RestTemplate class via the @Autowired annotation.
 * 
 * Metodologia de uso:
 * localhost:8081/v1/organization (POST) creando la organizacion
 * localhost:8080/v1/organization/0337e939-821a-420c-88ea-a94ecdab1fca/license (POST) creando la licencia para la organizacion con el ID creado antes
 * localhost:8080/v1/organization/0337e939-821a-420c-88ea-a94ecdab1fca/license/4d528bcc-1249-4c7c-9325-1e5f41a1a701/discovery (GET)
 * Le pegamos a licensing-service con id de organizacion y licencia, le pega a microservicio en 8081 sin que sepamos la URL! 
 * 
 */
@Component
public class DiscoveryClientMode {

	@Autowired
    private DiscoveryClient discoveryClient;

	@CircuitBreaker(name="organizationService")
	public Organization getOrganization(String organizationId) throws TimeoutException {
		
		// para probar el Circuit Breaker llamando a un microservicio
		//throw new TimeoutException();
		
        RestTemplate restTemplate = new RestTemplate();
        List<ServiceInstance> instances = discoveryClient.getInstances("organization-service");

        if (instances.size()==0) return null;
        // obtenemos el endpoint del servicio
        String serviceUri = String.format("%s/v1/organization/%s",instances.get(0).getUri().toString(), organizationId);
    
        // restTemplate STANDARD para llamar al servicio
        ResponseEntity< Organization > restExchange =
                restTemplate.exchange(
                        serviceUri,
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        return restExchange.getBody();
        
    }
}
