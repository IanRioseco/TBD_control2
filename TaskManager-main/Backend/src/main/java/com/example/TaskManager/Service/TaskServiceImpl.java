package com.example.TaskManager.Service;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.Entity.Sector;
import com.example.TaskManager.Entity.Task;
import com.example.TaskManager.Entity.User;
import com.example.TaskManager.Mapper.TaskMapper;
import com.example.TaskManager.Repository.SectorRepository;
import com.example.TaskManager.Repository.TaskRepository;
import com.example.TaskManager.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final TaskMapper taskMapper;

    @Override
    public List<TaskDTO> getTasksForUser(Long userId) {
        // Se ajustó el nombre del método en el repositorio a findByUser_Id
        List<Task> tasks = taskRepository.findByUser_Id(userId);
        return tasks.stream().map(taskMapper::toDto).toList();
    }

    @Override
    public TaskDTO createTask(TaskDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Sector sector = null;
        if (dto.getSectorId() != null) {
            sector = sectorRepository.findById(dto.getSectorId())
                    .orElseThrow(() -> new RuntimeException("Sector no encontrado"));
        }

        Task task = taskMapper.toEntity(dto, user, sector);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Sector sector = null;
        if (dto.getSectorId() != null) {
            sector = sectorRepository.findById(dto.getSectorId())
                    .orElseThrow(() -> new RuntimeException("Sector no encontrado"));
        }

        taskMapper.updateEntity(task, dto, user, sector);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // ===== Legacy API used by tests =====

    @Override
    public ResponseEntity<TaskDTO> create(TaskDTO dto, Long userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (dto == null || dto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }

        Sector sector = null;
        if (dto.getSectorId() != null) {
            sector = sectorRepository.findById(dto.getSectorId()).orElse(null);
        }

        Task entity = taskMapper.toEntity(dto, userOpt.get(), sector);
        Task saved = taskRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toDto(saved));
    }

    @Override
    public List<TaskDTO> findByUserId(Long userId) {
        return taskRepository.findAllByUserIdOrderByDateAsc(userId)
                .stream().map(taskMapper::toDto).toList();
    }

    @Override
    public ResponseEntity<TaskDTO> findById(Long id) {
        return taskRepository.findById(id)
                .map(t -> ResponseEntity.ok(taskMapper.toDto(t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Override
    public ResponseEntity<TaskDTO> update(Task task, Long userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (task == null || task.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!taskRepository.existsById(task.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        task.setUser(userOpt.get());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(taskMapper.toDto(saved));
    }

    @Override
    public ResponseEntity<TaskDTO> updateFinishedStatus(Long id) {
        var opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Task t = opt.get();
        t.setFinished(!t.isFinished());
        Task saved = taskRepository.save(t);
        return ResponseEntity.ok(taskMapper.toDto(saved));
    }

    @Override
    public List<TaskDTO> findByFinished(Long userId) {
        return taskRepository.findAllByFinishedTrueAndUserIdOrderByDateAsc(userId)
                .stream().map(taskMapper::toDto).toList();
    }

    @Override
    public List<TaskDTO> findByUnfinished(Long userId) {
        return taskRepository.findAllByFinishedFalseAndUserIdOrderByDateAsc(userId)
                .stream().map(taskMapper::toDto).toList();
    }

    @Override
    public List<TaskDTO> findByImportant(Long userId) {
        return taskRepository.findAllByImportantTrueAndUserIdOrderByDateAsc(userId)
                .stream().map(taskMapper::toDto).toList();
    }

    @Override
    public ResponseEntity<TaskDTO> deleteById(Long id) {
        var opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}