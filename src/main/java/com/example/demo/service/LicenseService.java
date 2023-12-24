package com.example.demo.service;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.demo.config.ServiceConfig;
import com.example.demo.model.License;
import com.example.demo.model.Organization;
import com.example.demo.repository.LicenseRepository;
import com.example.demo.service.client.DiscoveryClientMode;
import com.example.demo.service.client.FeignClientMode;
import com.example.demo.service.client.RestTemplateClient;

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
	
	//private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

	public License getLicense(String licenseId, String organizationId){
		License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
		if (null == license) {
			throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null),licenseId, organizationId));	
		}
		return license.withComment(config.getProperty());
	}
	
	//METODO PARA DIFERENTES CLIENTES!
	public License getLicense(String licenseId, String organizationId, String clientType){
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
	private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
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
			organization = restTemplateClient.getOrganization(organizationId);
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
	 */
	public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
		
		//logger.debug("getLicensesByOrganization Correlation id: {}",
				//UserContextHolder.getContext().getCorrelationId());
		//randomlyRunLong();
		return licenseRepository.findByOrganizationId(organizationId);
	}
	
}
