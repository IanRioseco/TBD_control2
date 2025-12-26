package com.example.TaskManager.Controller;

import com.example.TaskManager.DTO.SectorDTO;
import com.example.TaskManager.Entity.Sector;
import com.example.TaskManager.Repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectorController {

    private final SectorRepository sectorRepository;

    @GetMapping("/sectors")
    public ResponseEntity<List<SectorDTO>> getAllSectors() {
        List<Sector> sectors = sectorRepository.findAll();

        // Convertimos Entidad -> DTO manualmente para evitar errores con Point
        List<SectorDTO> dtos = sectors.stream().map(sector -> {
            Double lat = (sector.getLocation() != null) ? sector.getLocation().getY() : null;
            Double lon = (sector.getLocation() != null) ? sector.getLocation().getX() : null;

            return SectorDTO.builder()
                    .id(sector.getId())
                    .name(sector.getName())
                    .latitude(lat)
                    .longitude(lon)
                    .build();
        }).toList();

        return ResponseEntity.ok(dtos);
    }
}