package com.ucp.aseo_ucp_backend;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; // <-- AÑADIR IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync; // <-- AÑADIR IMPORT
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync 
public class AseoUcpBackendApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}
	
	public static void main(String[] args) {
		SpringApplication.run(AseoUcpBackendApplication.class, args);
	}

	// --- AÑADIR ESTE MÉTODO ---
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	// --------------------------
}