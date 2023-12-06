package com.example.demo.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.model.License;

@Service
public class LicenseService {

	public License getLicense(String licenseId, String organizationId) { 
		
		License license = new License();
		license.setId(new Random().nextInt(1000)); 
		license.setLicenseId(licenseId); 
		license.setOrganizationId(organizationId); 
		license.setDescription("Software product"); 
		license.setProductName("Ostock"); 
		license.setLicenseType("full");
		return license;
	   }
	
	public String createLicense(License license, String organizationId) { 
		String responseMessage = null;
		if(license != null) {
			license.setOrganizationId(organizationId);
			responseMessage = String.format("This is the post and the object is: %s", license.toString());
		}
		
		return responseMessage;
	}
	
	
}
