package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.License;
import com.example.demo.service.LicenseService;

@RestController
@RequestMapping(value="v1/organization/{organizationId}/license")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;
	
	// endpoint: v1/organization/{organizationId}/license/{licenseId}
	@GetMapping(value="/{licenseId}")
	public ResponseEntity<License> getLicense (
			@PathVariable("organizationId") String organizationId,
			@PathVariable("licenseId") String licenseId) {
		
		License license = licenseService.getLicense(licenseId, organizationId);
		
		return ResponseEntity.ok(license); // ResponseEntity represents the entire HTTP response
		
	}
	
	@PutMapping
	public ResponseEntity<String> updateLicense(
			@PathVariable("organizationId") String organizationId, 
			@RequestBody License request) {
		
		return ResponseEntity.ok(licenseService.updateLicense(request, organizationId));
	}
	
	@PostMapping
	public ResponseEntity<String> createLicense(
			@PathVariable("organizationId") String organizationId, 
			@RequestBody License request) {
		
		return ResponseEntity.ok(licenseService.createLicense(request, organizationId));
	}
	
}
