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

    private final Cloudinary cloudinary; 

    @Override
    public String storeFile(MultipartFile file) {
        try {
            Map result = upload(file);
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new FileStorageException("Fallo al guardar archivo " + file.getOriginalFilename(), e);
        }
    }

    public Map upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FileStorageException("Fallo al guardar archivo vacío.");
        }
        
        String publicId = UUID.randomUUID().toString();
        
        // --- OPTIMIZACIÓN DE MEMORIA ---
        // En lugar de file.getBytes(), usamos file.getInputStream()
        // Esto envía el archivo directamente a Cloudinary sin cargarlo todo en la RAM.
        Map uploadResult = cloudinary.uploader().upload(file.getInputStream(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "auto" 
                ));
        
        return uploadResult;
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public void init() {
        // No se necesita para Cloudinary
    }
}