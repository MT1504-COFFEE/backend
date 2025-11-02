package com.ucp.aseo_ucp_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;  
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
public class FileController { // <-- La clase principal COMIENZA aquí

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

            if (fileStorageService instanceof CloudinaryFileStorageServiceImpl) {
                CloudinaryFileStorageServiceImpl cloudinaryService = (CloudinaryFileStorageServiceImpl) fileStorageService;
                Map uploadResult = cloudinaryService.upload(file);
                
                String contentType = file.getContentType();
                String type = (contentType != null && contentType.startsWith("video")) ? "video" : "image";

                responseBody = Map.of(
                        "url", uploadResult.get("secure_url").toString(),
                        "publicId", uploadResult.get("public_id").toString(), 
                        "type", type,
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize()
                );
            } else {
                storedFileNameOrUrl = fileStorageService.storeFile(file);
                String fileDownloadUri = fileUrlResolver.resolve(storedFileNameOrUrl);
                String contentType = file.getContentType();
                String type = (contentType != null && contentType.startsWith("video")) ? "video" : "image";
                
                responseBody = Map.of(
                        "url", fileDownloadUri,
                        "publicId", storedFileNameOrUrl, 
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

    @DeleteMapping("/upload/{publicId}")
    public ResponseEntity<?> deleteFile(@PathVariable String publicId) {
        try {
            if (fileStorageService instanceof CloudinaryFileStorageServiceImpl) {
                CloudinaryFileStorageServiceImpl cloudinaryService = (CloudinaryFileStorageServiceImpl) fileStorageService;
                cloudinaryService.deleteFile(publicId);
            } else if (fileStorageService instanceof FileStorageServiceImpl) {
                // Lógica para borrar archivo local (si la implementas en FileStorageServiceImpl)
                // ((FileStorageServiceImpl) fileStorageService).deleteFile(publicId);
            }
            return ResponseEntity.ok(Map.of("message", "Archivo eliminado"));
        } catch (Exception e) {
            System.err.println("Error al borrar archivo: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo eliminar el archivo"));
        }
    }

} // <-- La clase principal FileController TERMINA aquí


// --- CLASES MOVIDAS FUERA DE FileController ---

/**
 * Interfaz para resolver la URL base de los archivos.
 * Spring inyectará la implementación correcta ('local' o 'prod')
 */
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