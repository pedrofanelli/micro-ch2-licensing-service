package com.example.demo.service;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.demo.config.ServiceConfig;
import com.example.demo.model.License;
import com.example.demo.repository.LicenseRepository;
import com.example.demo.service.client.DiscoveryClientMode;

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

	
}
