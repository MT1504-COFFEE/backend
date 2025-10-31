package com.ucp.aseo_ucp_backend.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resuelve la ruta absoluta del directorio de subida
        String resolvedPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        // Asegúrate de que la ruta para addResourceLocations sea correcta y termine con /
        String resourceLocation = "file:" + resolvedPath + "/";

        System.out.println("Configuring static resource handler: /uploads/** -> " + resourceLocation); // Log para depuración

        registry.addResourceHandler("/uploads/**") // La URL base
                .addResourceLocations(resourceLocation); // El directorio físico
    }
}