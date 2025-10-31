package com.ucp.aseo_ucp_backend.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // ¡Importa esto!
import org.springframework.context.annotation.Profile;

import com.cloudinary.Cloudinary;

@Configuration
@Profile("prod") // ¡Importante! Solo carga esto en producción (Railway)
public class CloudinaryConfig {

    // Inyectamos 3 variables separadas
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true"); // Usa https
        return new Cloudinary(config);
    }
}