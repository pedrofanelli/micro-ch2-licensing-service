package com.example.demo.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.model.License;

@Service
public class LicenseService {

	public License getLicense(String licenseId, String organizationId){ 
		
		License license = new License();
		license.setId(new Random().nextInt(1000)); 
		license.setLicenseId(licenseId); 
		license.setOrganizationId(organizationId); 
		license.setDescription("Software product"); 
		license.setProductName("Ostock"); 
		license.setLicenseType("full");
		return license;
	   }
}
