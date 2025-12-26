package com.example.TaskManager.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorDTO {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
}