package com.example.TaskManager.Controller;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.Entity.Sector;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Entity.User;
import com.example.TaskManager.Mapper.TaskMapper;
import com.example.TaskManager.Repository.SectorRepository;
import com.example.TaskManager.Repository.TaskRepository;
import com.example.TaskManager.Repository.UserRepository;
import com.example.TaskManager.Service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    // NUEVO: para validar usuario autenticado y aplicar sectorId correctamente
    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final TaskMapper taskMapper;

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

    /**
     * Endpoint principal de edición (lo usa tu TasksView y tu AdminView vía taskService.js)
     *
     * Arreglos:
     * - Aplica dto.sectorId (cambia ubicación)
     * - Si el que edita NO es admin: solo puede editar SUS tareas y NO puede reasignar userId
     * - Si el que edita ES admin: puede reasignar userId y sectorId
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTaskById(
            @PathVariable Long id,
            @RequestBody TaskDTO dto,
            @RequestParam(required = false) Long userId // lo dejamos por compatibilidad
    ) {
        var opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Task task = opt.get();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        // Usuario real del token (no confiar en userId param)
        User requester = userRepository.findByUsername(auth.getName()).orElse(null);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Si NO es admin: solo puede editar tareas propias
        if (!isAdmin) {
            if (task.getUser() == null || !task.getUser().getId().equals(requester.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // ====== Actualizar campos simples ======
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setFinished(dto.isFinished());
        task.setImportant(dto.isImportant());

        // ====== Reasignación de usuario ======
        // - USER normal: siempre queda como requester
        // - ADMIN: puede usar dto.userId (o mantener el actual si viene null)
        if (isAdmin) {
            Long targetUserId = dto.getUserId() != null ? dto.getUserId() : (task.getUser() != null ? task.getUser().getId() : null);
            if (targetUserId == null) {
                return ResponseEntity.badRequest().build();
            }
            User target = userRepository.findById(targetUserId).orElse(null);
            if (target == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            task.setUser(target);
        } else {
            task.setUser(requester);
        }

        // ====== CAMBIO CRÍTICO: aplicar sectorId ======
        // Si viene null, permitimos dejarlo null (o mantener el existente si prefieres)
        if (dto.getSectorId() != null) {
            Sector sector = sectorRepository.findById(dto.getSectorId()).orElse(null);
            if (sector == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            task.setSector(sector);
        }

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(taskMapper.toDto(saved));
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
