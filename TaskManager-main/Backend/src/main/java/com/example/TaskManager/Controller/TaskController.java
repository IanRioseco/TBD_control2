package com.example.TaskManager.Controller;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Service.TaskService;
import com.example.TaskManager.Repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    // GET /tasks?userId=1  -> lista de tareas del usuario
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.findByUserId(userId));
    }

    // POST /tasks  (en el body viene title, description, dueDate, important, userId, sectorId)
    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto, @RequestParam Long userId) {
        return taskService.create(dto, userId);
    }

    @PutMapping("/tasks")
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskDTO dto, @RequestParam Long userId) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setFinished(dto.isFinished());
        task.setImportant(dto.isImportant());
        return taskService.update(task, userId);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTaskById(@PathVariable Long id, @RequestBody TaskDTO dto, @RequestParam Long userId) {
        // Obtener la tarea existente y actualizar sus campos
        Task task = taskRepository.findById(id).orElseGet(Task::new);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setFinished(dto.isFinished());
        task.setImportant(dto.isImportant());
        return taskService.update(task, userId);
    }

    @PatchMapping("/tasks/{id}/toggle")
    public ResponseEntity<TaskDTO> toggleFinished(@PathVariable Long id) {
        return taskService.updateFinishedStatus(id);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return taskService.deleteById(id);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @GetMapping("/tasks/finished")
    public ResponseEntity<List<TaskDTO>> getFinished(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.findByFinished(userId));
    }

    @GetMapping("/tasks/unfinished")
    public ResponseEntity<List<TaskDTO>> getUnfinished(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.findByUnfinished(userId));
    }

    @GetMapping("/tasks/important")
    public ResponseEntity<List<TaskDTO>> getImportant(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.findByImportant(userId));
    }
}
