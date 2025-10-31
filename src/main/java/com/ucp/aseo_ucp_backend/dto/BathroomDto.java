package com.ucp.aseo_ucp_backend.dto;

import com.ucp.aseo_ucp_backend.entity.Bathroom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BathroomDto {
    private Long id;
    private String name;
    private Integer floor; // Número de piso, como definido en el README
    private String building; // Nombre del edificio, como definido en el README

    public static BathroomDto fromEntity(Bathroom bathroom) {
        if (bathroom == null) return null;

        Integer floorNumber = null;
        if (bathroom.getFloor() != null) {
            floorNumber = bathroom.getFloor().getFloorNumber();
        }

        String buildingName = null;
        // El schema del README para bathrooms tiene building_id directo,
        // así que podemos obtener el nombre desde ahí si la relación está cargada.
        if (bathroom.getBuilding() != null) {
             buildingName = bathroom.getBuilding().getName();
        }
         // Alternativa si solo tienes floor_id y floor tiene building_id:
         /* else if (bathroom.getFloor() != null && bathroom.getFloor().getBuilding() != null) {
              buildingName = bathroom.getFloor().getBuilding().getName();
         } */


        return new BathroomDto(
            bathroom.getId(),
            bathroom.getName(),
            floorNumber,
            buildingName
        );
    }
}