package com.achhecode.jobpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.achhecode.playwright.config.PlaywrightCoreConfiguration;

@SpringBootApplication
@Import(PlaywrightCoreConfiguration.class)
public class JobPilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobPilotApplication.class, args);
	}

}
