package com.ucp.aseo_ucp_backend;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // <-- AÑADIR IMPORT

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync // <-- AÑADIR ESTA ANOTACIÓN
public class AseoUcpBackendApplication {

// --- AÑADIR ESTE MÉTODO ---
	/**
	 * Configura la zona horaria por defecto de la aplicación a "America/Bogota".
	 * Esto asegura que todos los 'LocalDateTime.now()' usen la hora de Colombia.
	 */
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}
	// --------------------------

	public static void main(String[] args) {
		SpringApplication.run(AseoUcpBackendApplication.class, args);
	}
}
