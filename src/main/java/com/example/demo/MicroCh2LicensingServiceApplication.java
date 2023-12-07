package com.example.demo;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;


/**
 * 
 * El esqueleto se arma en chapter 2, pero sigue en chapter 3
 * 
 * Core initialization logic for the microservice should be placed in this class.
 * 
 * @author peter
 *
 */
@SpringBootApplication
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

}
