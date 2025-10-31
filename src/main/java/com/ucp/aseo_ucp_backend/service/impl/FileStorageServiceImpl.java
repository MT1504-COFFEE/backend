package com.ucp.aseo_ucp_backend.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ucp.aseo_ucp_backend.exception.FileStorageException;
import com.ucp.aseo_ucp_backend.service.FileStorageService;

import jakarta.annotation.PostConstruct;

@Service
@Profile("local") // Se activa SÓLO en perfil local
public class FileStorageServiceImpl implements FileStorageService { // <-- Esta es tu clase principal

    private final Path rootLocation;

    // Inyecta la ruta desde application.properties
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
         if (uploadDir == null || uploadDir.isBlank()) {
             // Define un directorio por defecto si no está configurado
             this.rootLocation = Paths.get("./uploads").toAbsolutePath().normalize();
             System.out.println("ADVERTENCIA: file.upload-dir no configurado, usando directorio por defecto: " + this.rootLocation);
        } else {
             this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        }

        System.out.println("Directorio de subida configurado en: " + this.rootLocation);
    }


    @Override
    @PostConstruct // Se ejecuta después de crear el bean
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Directorio de subida inicializado/verificado: " + rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("No se pudo inicializar el directorio de almacenamiento de archivos.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normaliza el nombre del archivo original
        String originalFilename = file.getOriginalFilename();
        
        // --- CORRECCIÓN DE NULOS (que hicimos antes) ---
        if (file.isEmpty()) {
            throw new FileStorageException("Fallo al guardar archivo vacío.");
        }
        if (!StringUtils.hasText(originalFilename)) {
             throw new FileStorageException("El nombre del archivo original está vacío o es nulo.");
        }
        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        // ---------------------------------------------
        
        try {
            if (cleanedFilename.contains("..")) {
                // Ataque de path traversal
                throw new FileStorageException(
                        "No se puede guardar archivo con ruta relativa fuera del directorio actual " + cleanedFilename);
            }

            // Genera un nombre único
            String fileExtension = "";
            int lastDotIndex = cleanedFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = cleanedFilename.substring(lastDotIndex);
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path destinationFile = this.rootLocation.resolve(uniqueFilename).normalize();

             // Asegura que el archivo se guarde dentro del directorio raíz permitido
            if (!destinationFile.getParent().equals(this.rootLocation.normalize())) {
                throw new FileStorageException(
                        "No se puede guardar archivo fuera del directorio raíz configurado.");
            }

            // Copia el contenido del archivo subido al archivo destino
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Archivo guardado exitosamente en: " + destinationFile);
            return uniqueFilename; // Devuelve solo el nombre único generado

        } catch (IOException e) {
            throw new FileStorageException("Fallo al guardar archivo " + cleanedFilename, e);
        }
    }

    // --- 3. ELIMINA EL BLOQUE DUPLICADO QUE TENÍAS AQUÍ ---
    /*
    @Service
    @Profile("local") 
    public class FileStorageServiceImpl implements FileStorageService {
        // ... (código duplicado) ...
    }
    */
    
    // Implementa los otros métodos (load, loadAsResource, etc.) si los necesitas
}