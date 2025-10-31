package com.ucp.aseo_ucp_backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile; // Para construir URL

import com.ucp.aseo_ucp_backend.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    // Inyecta la URL base desde application.properties
    private String fileUrlBase;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // ... (código de validación)
        try {
            // storeFile() ahora devuelve la URL COMPLETA de Cloudinary
            String fileDownloadUri = fileStorageService.storeFile(file);

            // ... (código para determinar el 'type')
            String contentType = file.getContentType();
            String type = "image";
            if (contentType != null && contentType.startsWith("video")) {
                type = "video";
            }

            // Devuelve la respuesta JSON con la URL completa
            return ResponseEntity.ok(Map.of(
                    "url", fileDownloadUri, // <-- Esta es la URL completa
                    "type", type,
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize()
            ));

        } catch (Exception e) {
             System.err.println("Error al subir archivo: " + e.getMessage());
             e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo subir el archivo. Error: " + e.getMessage()));
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