package com.example.TaskManager.Controller;

import com.example.TaskManager.DTO.SectorCountProjection;
import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.DTO.UserSectorCountProjection;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Mapper.TaskMapper;
import com.example.TaskManager.Repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @GetMapping("/user/{userId}/tasks-per-sector")
    public List<SectorCountProjection> tasksPerSector(@PathVariable Long userId) {
        return taskRepository.countCompletedBySectorForUser(userId);
    }

    @GetMapping("/user/{userId}/nearest-pending")
    public TaskDTO nearestPending(@PathVariable Long userId) {
        Task task = taskRepository.findNearestPendingTaskForUser(userId);
        // Convertimos a DTO para evitar errores de JSON y que el frontend lo lea bien
        return taskMapper.toDto(task);
    }

    @GetMapping("/user/{userId}/top-sector-2km")
    public SectorCountProjection topSector2km(@PathVariable Long userId) {
        return taskRepository.topCompletedSectorNearUser(userId, 2000);
    }

    @GetMapping("/user/{userId}/top-sector-5km")
    public SectorCountProjection topSector5km(@PathVariable Long userId) {
        return taskRepository.topCompletedSectorNearUser(userId, 5000);
    }

    @GetMapping("/user/{userId}/avg-distance-completed")
    public Double avgDistance(@PathVariable Long userId) {
        return taskRepository.avgDistanceCompletedTasksForUser(userId);
    }

    @GetMapping("/pending-by-sector")
    public List<SectorCountProjection> pendingBySector() {
        return taskRepository.pendingTasksBySector();
    }

    @GetMapping("/completed-by-user-and-sector")
    public List<UserSectorCountProjection> completedByUserAndSector() {
        return taskRepository.completedTasksByUserAndSector();
    }
}