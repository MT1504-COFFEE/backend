package com.ucp.aseo_ucp_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void init(); // Podría crear el directorio si no existe
    String storeFile(MultipartFile file);
    // Stream<Path> loadAll(); // Si necesitas listar archivos
    // Path load(String filename); // Si necesitas la ruta física
    // Resource loadAsResource(String filename); // Si quieres servir archivos desde Spring
    // void deleteFile(String filename); // Si necesitas borrar archivos
}