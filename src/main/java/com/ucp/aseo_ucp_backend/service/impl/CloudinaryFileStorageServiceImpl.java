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
@Primary // Prefiere este servicio
@Profile("prod") // Se activa SÓLO en producción (en Railway)
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Fallo al guardar archivo vacío.");
            }
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", UUID.randomUUID().toString(),
                            "resource_type", "auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new FileStorageException("Fallo al guardar archivo " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void init() {
        // No se necesita
    }
}