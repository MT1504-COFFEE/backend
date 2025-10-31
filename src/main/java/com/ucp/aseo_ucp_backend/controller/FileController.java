package com.ucp.aseo_ucp_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity; // Importa @Value
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // Para construir URL
import org.springframework.web.multipart.MultipartFile;

import com.ucp.aseo_ucp_backend.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    // Inyecta la URL base desde application.properties
    @Value("${file.upload-url-base}")
    private String fileUrlBase;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo no puede estar vacío"));
        }

        try {
            // Guarda el archivo y obtiene el nombre único generado
            String filename = fileStorageService.storeFile(file);

            // Construye la URL pública completa
            // Asegúrate que fileUrlBase termine con '/'
            String fileDownloadUri = fileUrlBase + filename;

            // Determina el tipo (imagen o video) basado en el ContentType
            String contentType = file.getContentType();
            String type = "image"; // Por defecto
            if (contentType != null && contentType.startsWith("video")) {
                type = "video";
            }

            // Devuelve la respuesta JSON esperada por el frontend
            return ResponseEntity.ok(Map.of(
                    "url", fileDownloadUri,
                    "type", type,
                    "filename", file.getOriginalFilename(), // Nombre original para mostrar
                    "size", file.getSize()
            ));

        } catch (Exception e) {
            // Loguea el error real en un sistema de logging
             System.err.println("Error al subir archivo: " + e.getMessage());
            // Devuelve un error genérico al cliente
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo subir el archivo. Inténtalo de nuevo más tarde."));
        }
    }

    // Si necesitas servir archivos directamente desde Spring (alternativa a MvcConfig)
    /*
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        // ... Lógica para determinar ContentType y devolver el archivo ...
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    */
}