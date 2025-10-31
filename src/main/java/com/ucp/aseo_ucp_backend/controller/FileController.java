package com.ucp.aseo_ucp_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile; // Importamos Value
import org.springframework.http.ResponseEntity; // Importamos Profile
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ucp.aseo_ucp_backend.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    // Objeto interno para manejar la URL base (diferente en local vs prod)
    private final FileUrlResolver fileUrlResolver;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo no puede estar vacío"));
        }
        try {
            // Guarda el archivo (en disco local o en Cloudinary)
            String storedFileNameOrUrl = fileStorageService.storeFile(file);

            // Resuelve la URL final
            String fileDownloadUri = fileUrlResolver.resolve(storedFileNameOrUrl);
            
            String contentType = file.getContentType();
            String type = (contentType != null && contentType.startsWith("video")) ? "video" : "image";

            return ResponseEntity.ok(Map.of(
                    "url", fileDownloadUri, // Esta es la URL pública correcta
                    "type", type,
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize()
            ));
        } catch (Exception e) {
             System.err.println("Error al subir archivo: " + e.getMessage());
             e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo subir el archivo: " + e.getMessage()));
        }
    }
}

// Clase de ayuda para resolver la URL
interface FileUrlResolver {
    String resolve(String fileNameOrUrl);
}

// Implementación para "local" (usa la variable de entorno)
@Component
@Profile("local")
class LocalFileUrlResolver implements FileUrlResolver {
    @Value("${file.upload-url-base:http://localhost:8080/uploads/}") // Valor por defecto
    private String fileUrlBase;
    
    @Override
    public String resolve(String fileName) {
        return fileUrlBase + fileName;
    }
}

// Implementación para "prod" (devuelve la URL de Cloudinary directamente)
@Component
@Profile("prod")
class ProdFileUrlResolver implements FileUrlResolver {
    @Override
    public String resolve(String fullUrl) {
        return fullUrl; // El servicio de Cloudinary ya devolvió la URL completa
    }
}