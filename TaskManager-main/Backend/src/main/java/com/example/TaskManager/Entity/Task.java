package com.example.TaskManager.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

import java.time.LocalDate;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // Se renombra a dueDate para coincidir con DTO y Mapper y solucionar el error "cannot find symbol getDueDate"
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Default
    @Column(nullable = false)
    private boolean finished = false;

    @Default
    @Column(nullable = false)
    private boolean important = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Custom method to support tests calling builder().date(null)
    public static class TaskBuilder {
        public TaskBuilder date(java.util.Date date) {
            if (date == null) {
                // Keep dueDate as null when no date provided
                return this;
            }
            java.time.LocalDate local = date.toInstant()
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate();
            return this.dueDate(local);
        }
    }
}