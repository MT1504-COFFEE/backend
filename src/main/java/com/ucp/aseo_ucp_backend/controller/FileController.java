package com.ucp.aseo_ucp_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping; // Importar
import org.springframework.web.bind.annotation.PathVariable;  // Importar
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ucp.aseo_ucp_backend.service.FileStorageService;
import com.ucp.aseo_ucp_backend.service.impl.CloudinaryFileStorageServiceImpl;
import com.ucp.aseo_ucp_backend.service.impl.FileStorageServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    // Inyectamos la interfaz genérica. Spring decidirá si usa la Local o la de Cloudinary
    // basado en el perfil activo (ej. 'prod' en Railway).
    private final FileStorageService fileStorageService; 
    private final FileUrlResolver fileUrlResolver;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo no puede estar vacío"));
        }
        try {
            String storedFileNameOrUrl;
            Map<String, Object> responseBody;

            // Verificamos si el servicio inyectado es el de Cloudinary
            if (fileStorageService instanceof CloudinaryFileStorageServiceImpl) {
                CloudinaryFileStorageServiceImpl cloudinaryService = (CloudinaryFileStorageServiceImpl) fileStorageService;
                
                // Usamos el método 'upload' que devuelve el Mapa completo
                Map uploadResult = cloudinaryService.upload(file);
                
                String contentType = file.getContentType();
                String type = (contentType != null && contentType.startsWith("video")) ? "video" : "image";

                responseBody = Map.of(
                        "url", uploadResult.get("secure_url").toString(),
                        "publicId", uploadResult.get("public_id").toString(), // ENVIAMOS EL publicId
                        "type", type,
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize()
                );
            } else {
                // Lógica 'local' (si estás en tu máquina)
                storedFileNameOrUrl = fileStorageService.storeFile(file);
                String fileDownloadUri = fileUrlResolver.resolve(storedFileNameOrUrl);
                String contentType = file.getContentType();
                String type = (contentType != null && contentType.startsWith("video")) ? "video" : "image";
                
                responseBody = Map.of(
                        "url", fileDownloadUri,
                        "publicId", storedFileNameOrUrl, // En local, el publicId es el nombre del archivo
                        "type", type,
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize()
                );
            }

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
             System.err.println("Error al subir archivo: " + e.getMessage());
             e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo subir el archivo: " + e.getMessage()));
        }
    }

    // Endpoint para BORRAR
    @DeleteMapping("/upload/{publicId}")
    public ResponseEntity<?> deleteFile(@PathVariable String publicId) {
        try {
            // De nuevo, verificamos si estamos en 'prod' (Cloudinary)
            if (fileStorageService instanceof CloudinaryFileStorageServiceImpl) {
                CloudinaryFileStorageServiceImpl cloudinaryService = (CloudinaryFileStorageServiceImpl) fileStorageService;
                cloudinaryService.deleteFile(publicId);
            } else if (fileStorageService instanceof FileStorageServiceImpl) {
                // Aquí iría la lógica para borrar el archivo local
                // ((FileStorageServiceImpl) fileStorageService).deleteFile(publicId);
            }
            return ResponseEntity.ok(Map.of("message", "Archivo eliminado"));
        } catch (Exception e) {
            System.err.println("Error al borrar archivo: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo eliminar el archivo"));
        }
    }


    // --- El resto de tu archivo (FileUrlResolver) se queda igual ---
    // (Asegúrate de que estas clases estén al final del archivo)
    interface FileUrlResolver {
        String resolve(String fileNameOrUrl);
    }
    
    @Component
    @Profile("local")
    class LocalFileUrlResolver implements FileUrlResolver {
        @Value("${file.upload-url-base:http://localhost:8080/uploads/}") 
        private String fileUrlBase;
        
        @Override
        public String resolve(String fileName) {
            return fileUrlBase + fileName;
        }
    }
    
    @Component
    @Profile("prod")
    class ProdFileUrlResolver implements FileUrlResolver {
        @Override
        public String resolve(String fullUrl) {
            return fullUrl; // Cloudinary ya da la URL completa
        }
    }
}