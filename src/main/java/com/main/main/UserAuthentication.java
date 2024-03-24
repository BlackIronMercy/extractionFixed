package com.main.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@ComponentScan(basePackages = {"com.*"})
@EnableJpaRepositories(basePackages = {"com.main.repo"})
@EntityScan("com.main.model")
@EnableScheduling
public class UserAuthentication extends SpringBootServletInitializer {

	 private static final Logger log = LogManager.getLogger(UserAuthentication.class);
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(UserAuthentication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UserAuthentication.class, args);
    }
    
    public void run(ApplicationArguments applicationArguments) throws Exception {
    	log.debug("Log testing for debug");
    	log.info("Log testing for Info ");
    	log.warn("Log testing for warning!");
    	log.error("Log testing for  Error.");
    	log.fatal("Log testing for fatal");
    }
}
