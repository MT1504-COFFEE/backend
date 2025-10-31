// Archivo: src/main/java/com/ucp/aseo_ucp_backend/repository/BathroomRepository.java
package com.ucp.aseo_ucp_backend.repository; // <- Tu paquete de repositorios

import java.util.List; // <- Tu paquete de entidades

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository; // <- ¡Importa @Repository!

import com.ucp.aseo_ucp_backend.entity.Bathroom;

@Repository // <- ¡Asegúrate que esta anotación esté presente!
public interface BathroomRepository extends JpaRepository<Bathroom, Long> {

    // Carga Baños junto con Pisos y Edificios en una sola consulta
    @Query("SELECT b FROM Bathroom b LEFT JOIN FETCH b.floor f LEFT JOIN FETCH b.building bu ORDER BY bu.name, f.floorNumber, b.name")
    List<Bathroom> findAllWithDetails();

}