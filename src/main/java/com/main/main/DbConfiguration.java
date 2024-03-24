package com.main.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("spring.datasource")
@SuppressWarnings("unused")
public class DbConfiguration {


	@Profile("dev")
	@Bean
	public String devDatabaseConnection() {
		
		System.out.println("1.. dev..");
		return "dev connection is made";
	}
	
	@Profile("uat")
	@Bean
	public String uatDatabaseConnection() {
		
		System.out.println("2.. uat..");
		return "uat connection is made";
	}
	
	
	@Profile("prod")
	@Bean
	public String prodDatabaseConnection() {
		
		System.out.println("3.. prod..");
		return "prod connection is made";
	}
	
	
}
