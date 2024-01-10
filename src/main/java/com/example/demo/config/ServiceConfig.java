package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

//@Configuration
//@ConfigurationProperties(prefix = "example")
@Component @Getter //@Setter
public class ServiceConfig{

  //private String property;
  
  @Value("${example.property}")
  private String property;
    
  @Value("${redis.server}")
  private String redisServer="";

  @Value("${redis.port}")
  private String redisPort="";
    
}
