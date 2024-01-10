package com.example.demo.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Organization;
import com.example.demo.repository.OrganizationRedisRepository;
import com.example.demo.utils.UserContext;

public class OrganizationRestTemplateClient {
	
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	OrganizationRedisRepository redisRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);
	
	public Organization getOrganization(String organizationId){
		logger.debug("In Licensing Service.getOrganization: {}", UserContext.getCorrelationId());

        Organization organization = checkRedisCache(organizationId);

        if (organization != null){
            logger.debug("I have successfully retrieved an organization {} from the redis cache: {}", organizationId, organization);
            return organization;
        }

        logger.debug("Unable to locate organization from the redis cache: {}.", organizationId);
        
		ResponseEntity<Organization> restExchange =
				restTemplate.exchange(
						"http://gateway:8072/organization/v1/organization/{organizationId}",
						HttpMethod.GET,
						null, Organization.class, organizationId);
		
		/*Save the record from cache*/
        organization = restExchange.getBody();
        if (organization != null) {
            cacheOrganizationObject(organization);
        }

		return restExchange.getBody();
	}
}
