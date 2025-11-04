package com.ucp.aseo_ucp_backend.config;

import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <-- AÑADIR IMPORT

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); 
        
        // --- AÑADIR ESTA LÍNEA ---
        // Asegura que la serialización de JSON también use la zona horaria de Bogota
        mapper.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        // --------------------------

        return mapper;
    }
}