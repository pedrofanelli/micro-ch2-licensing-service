package com.example.demo.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Organization;
import java.util.List;

/**
 * The Spring Discovery Client offers the lowest level of access to the Load Balancer and the 
 * services registered within it. Using the Discovery Client, you can query for all the services 
 * registered with the Spring Cloud Load Balancer client and their corresponding URLs.
 */
@Component
public class DiscoveryClientMode {

	@Autowired
    private DiscoveryClient discoveryClient;

	public Organization getOrganization(String organizationId) {
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
