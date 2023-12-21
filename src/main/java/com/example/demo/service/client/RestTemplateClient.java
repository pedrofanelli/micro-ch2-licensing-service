package com.example.demo.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Organization;

/**
 * We’ll see an example of how to use a REST template that’s Load Balancer–aware. 
 * This is one of the more common mechanisms for interacting with the Load Balancer via Spring. 
 * To use a Load Balancer–aware RestTemplate class, we need to define a RestTemplate bean with a 
 * Spring Cloud @LoadBalanced annotation. (in the main class we define the bean)
 * 
 * Rather than using the physical location of the service in the Rest-Template call, you need to build 
 * the target URL using the Eureka service ID of the service you want to call. 
 * 
 * The Load Balancer–enabled RestTemplate class parses the URL passed into it and uses whatever is 
 * passed in as the server name as the key to query the Load Balancer for an instance of a service. 
 * The actual service location and port are entirely abstracted from the developer. Also, by using 
 * the RestTemplate class, the Spring Cloud Load Balancer will round-robin load balance all requests 
 * among all the service instances.
 */
@Component
public class RestTemplateClient {

	@Autowired
    RestTemplate restTemplate;
	
	public Organization getOrganization(String organizationId){
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://organization-service/v1/organization/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        return restExchange.getBody();
    }
}
