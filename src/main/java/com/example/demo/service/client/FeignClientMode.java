package com.example.demo.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.model.Organization;

/**
 * The Feign library takes a different approach to call a REST service. With this approach, the 
 * developer first defines a Java interface and then adds Spring Cloud annotations to map what 
 * Eureka-based service the Spring Cloud Load Balancer will invoke. The Spring Cloud framework will 
 * dynamically generate a proxy class to invoke the targeted REST service. There’s no code written 
 * for calling the service other than an interface definition.
 * 
 * To enable the Feign client for use in our licensing service, we need to add a new 
 * annotation, @EnableFeignClients in the main class.
 */
@FeignClient("organization-service") // identifies your service to Feign
public interface FeignClientMode {

	@GetMapping(
            value="/v1/organization/{organizationId}", // path to endpoint
            consumes="application/json")
    Organization getOrganization(@PathVariable("organizationId") String organizationId); // parameters to endpoint
}
