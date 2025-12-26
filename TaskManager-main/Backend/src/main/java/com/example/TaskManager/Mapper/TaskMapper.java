package com.example.TaskManager.Mapper;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.Entity.Sector;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDTO toDto(Task task) {
        if (task == null) return null;

        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate()); // Ahora funciona
        dto.setFinished(task.isFinished());
        dto.setImportant(task.isImportant());

        if (task.getUser() != null) {
            // Corrección: La entidad User tiene getId(), no getUserId()
            dto.setUserId(task.getUser().getId());
        }
        if (task.getSector() != null) {
            dto.setSectorId(task.getSector().getId());
            dto.setSectorName(task.getSector().getName());
        }

        return dto;
    }

    // Static helper used by legacy tests
    public static TaskDTO toTaskDTO(Task task) {
        if (task == null) return null;
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setFinished(task.isFinished());
        dto.setImportant(task.isImportant());
        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
        }
        if (task.getSector() != null) {
            dto.setSectorId(task.getSector().getId());
            dto.setSectorName(task.getSector().getName());
        }
        return dto;
    }

    public Task toEntity(TaskDTO dto, User user, Sector sector) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setFinished(dto.isFinished());
        task.setImportant(dto.isImportant());
        task.setUser(user);
        task.setSector(sector);
        return task;
    }

    public void updateEntity(Task task, TaskDTO dto, User user, Sector sector) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setFinished(dto.isFinished());
        task.setImportant(dto.isImportant());
        task.setUser(user);
        task.setSector(sector);
    }
}