package com.example.TaskManager.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "sector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del sector (ej: "Construcción", "Reparación de semáforos", etc.)
    @Column(nullable = false, unique = true)
    private String name;

    // Punto geoespacial del sector
    @Column(name = "location", columnDefinition = "geometry(Point,4326)")
    private Point location;
}
