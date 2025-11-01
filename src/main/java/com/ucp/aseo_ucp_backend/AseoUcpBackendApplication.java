package com.ucp.aseo_ucp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // <-- AÑADIR ESTA ANOTACIÓN
public class AseoUcpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AseoUcpBackendApplication.class, args);
	}

}
