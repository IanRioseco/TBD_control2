package com.example.TaskManager.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean finished;
    private boolean important;

    private Long userId;
    private Long sectorId;
    private String sectorName;

    // Explicit getters to satisfy tests expecting getFinished()/getImportant()
    public boolean getFinished() {
        return finished;
    }

    public boolean getImportant() {
        return important;
    }

    // Also provide boolean-style getters for callers expecting isXxx()
    public boolean isFinished() {
        return finished;
    }

    public boolean isImportant() {
        return important;
    }

    // Custom method to support tests calling builder().date(null)
    public static class TaskDTOBuilder {
        public TaskDTOBuilder date(java.util.Date date) {
            if (date == null) {
                return this;
            }
            java.time.LocalDate local = date.toInstant()
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate();
            return this.dueDate(local);
        }
    }
}
