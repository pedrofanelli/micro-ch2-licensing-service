package com.example.demo;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.example.demo.utils.UserContextInterceptor;


/**
 * 
 * El esqueleto se arma en chapter 2, pero sigue en chapter 3
 * 
 * Core initialization logic for the microservice should be placed in this class.
 * 
 * Los beans de mas abajo son para internacionalizacion, es decir, usar properties para cambiar texto devuelto en funcion de idioma
 * 
 * HATEOAS se usa para agregar links al objeto que se devuelve. Los links relacionados al recurso.
 * 
 * Actuator se usa para obtener metricas y datos del estado del microservicio (data en application.properties)
 * 
 * El @RefreshScope permite refrescar los archivos de configuracion, sin tener que redeployar el Config Server, entonces
 * uno cambia el config, le pegamos con POST a localhost:8080/actuator/refresh, y eso refresca AMBOS! MAGIA!
 * 
 * 
 * @author peter
 *
 */

@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient
@EnableFeignClients
public class MicroCh2LicensingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroCh2LicensingServiceApplication.class, args);
	}
	
	/**
	 * LocalResolver y ResourceBundleMessageSource se usan para internacionalizacion, es decir,
	 * para que se realice una respuesta segun el idioma que se manda en el RequestHeader
	 * 
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		return localeResolver;
	}
	
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setUseCodeAsDefaultMessage(true); // does not throw an error if a message is not found, instead it returns the message code
		messageSource.setBasenames("messages"); // base name of the languages properties files
		return messageSource;
	}
	
	/**
	 * Get's a list of all the instances of the services
	 * Then it's autowired by the client (RestTemplateClient.java)
	 * @return
	 * 
	 * Agregamos un Interceptor para toda llamada REST
	 * 
	 */
	@SuppressWarnings("unchecked")
	@LoadBalanced
	@Bean
	public RestTemplate getRestTemplate(){
		RestTemplate template = new RestTemplate();
        List interceptors = template.getInterceptors();
        if (interceptors==null){
            template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        }
        else{
            interceptors.add(new UserContextInterceptor());
            template.setInterceptors(interceptors);
        }

        return template;
	}

}
