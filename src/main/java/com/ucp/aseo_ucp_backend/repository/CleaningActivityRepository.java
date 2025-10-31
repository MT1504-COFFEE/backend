package com.ucp.aseo_ucp_backend.repository; // <- Paquete correcto

import java.util.List; // <- Importa la entidad

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.CleaningActivity;

@Repository // <- Anotación importante
public interface CleaningActivityRepository extends JpaRepository<CleaningActivity, Long> {
    // No necesitas añadir métodos básicos como save() o findAll(), JpaRepository los provee.

    @Query("SELECT ca FROM CleaningActivity ca " +
       "LEFT JOIN FETCH ca.user u " +
       "LEFT JOIN FETCH ca.bathroom b " +
       "LEFT JOIN FETCH b.building bu " +
       "LEFT JOIN FETCH b.floor fl " + // Si necesitas datos del piso
       "LEFT JOIN FETCH ca.areasCleaned ac " +
       "LEFT JOIN FETCH ca.suppliesRefilled sr " +
       "ORDER BY ca.createdAt DESC")
    List<CleaningActivity> findAllWithDetails();
}