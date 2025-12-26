package com.example.TaskManager.Controller;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.DTO.UserDTO;
import com.example.TaskManager.Entity.User;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Mapper.TaskMapper;
import com.example.TaskManager.Repository.TaskRepository;
import com.example.TaskManager.Repository.UserRepository;
import com.example.TaskManager.Repository.SectorRepository;
import com.example.TaskManager.Service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SectorRepository sectorRepository;
    private final TaskService taskService;

    @GetMapping("/users")
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream().map(user ->
                UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .role(user.getRole().name())
                        .address(user.getAddress())
                        .latitude(user.getLatitude())
                        .longitude(user.getLongitude())
                        .build()
        ).toList();
    }

    @GetMapping("/tasks")
    public List<TaskDTO> listTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createTaskForUser(@RequestBody TaskDTO dto) {
        // El admin crea una tarea y la asigna a un usuario específico
        // El dto debe contener userId
        return taskService.create(dto, dto.getUserId());
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTaskForUser(@PathVariable Long id, @RequestBody TaskDTO dto) {
        // El admin actualiza una tarea existente (puede reasignarla a otro usuario)
        Task task = taskRepository.findById(id)
                .orElseGet(() -> new Task());
        
        // Actualizar campos
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setImportant(dto.isImportant());
        task.setFinished(dto.isFinished());
        
        // Asignar usuario
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            task.setUser(user);
        }
        
        // Asignar sector
        if (dto.getSectorId() != null) {
            var sector = sectorRepository.findById(dto.getSectorId()).orElse(null);
            task.setSector(sector);
        }
        
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(taskMapper.toDto(saved));
    }
}