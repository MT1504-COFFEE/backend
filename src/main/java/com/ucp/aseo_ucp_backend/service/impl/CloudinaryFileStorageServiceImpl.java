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
@Primary // Le dice a Spring que prefiera este servicio
@Profile("prod") // Solo activa este servicio cuando el perfil sea "producción"
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Fallo al guardar archivo vacío.");
            }

            // Genera un nombre público único
            String publicId = UUID.randomUUID().toString();

            // Sube el archivo a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "auto" // Detecta automáticamente si es imagen o video
                    ));

            // Cloudinary devuelve la URL segura (https)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new FileStorageException("Fallo al guardar archivo " + file.getOriginalFilename(), e);
        }
    }

    // init() no es necesario para Cloudinary
    @Override
    public void init() {
        // No se necesita crear directorios locales
    }
}