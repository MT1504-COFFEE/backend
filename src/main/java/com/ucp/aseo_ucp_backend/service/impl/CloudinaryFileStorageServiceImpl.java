package com.ucp.aseo_ucp_backend.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ucp.aseo_ucp_backend.exception.FileStorageException;
import com.ucp.aseo_ucp_backend.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Primary 
@Profile("prod") 
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary; // Esto se inyecta desde CloudinaryConfig

    // Este método solo devuelve la URL (como pide la interfaz)
    @Override
    public String storeFile(MultipartFile file) {
        try {
            Map result = upload(file); // Llama a nuestro nuevo método
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new FileStorageException("Fallo al guardar archivo " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Sube el archivo a Cloudinary y devuelve el Mapa de respuesta completo.
     * Este es el método que usa el FileController.
     */
    public Map upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FileStorageException("Fallo al guardar archivo vacío.");
        }
        
        String publicId = UUID.randomUUID().toString();
        
        // --- CORRECCIÓN AQUÍ ---
        // Se añade '()' a 'uploader'
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "auto" // Detecta si es imagen o video
                ));
        
        return uploadResult;
    }

    /**
     * Borra un archivo de Cloudinary usando su publicId.
     */
    public void deleteFile(String publicId) throws IOException {
        // --- CORRECCIÓN AQUÍ ---
        // Se añade '()' a 'uploader'
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public void init() {
        // No se necesita para Cloudinary
    }
}