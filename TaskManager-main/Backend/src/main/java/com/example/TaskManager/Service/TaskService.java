package com.example.TaskManager.Service;

import com.example.TaskManager.DTO.TaskDTO;
import com.example.TaskManager.Entity.Task;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TaskService {

    List<TaskDTO> getTasksForUser(Long userId);

    TaskDTO createTask(TaskDTO dto);

    TaskDTO updateTask(Long id, TaskDTO dto);

    void deleteTask(Long id);

    // Legacy API used by tests/controllers
    ResponseEntity<TaskDTO> create(TaskDTO dto, Long userId);
    List<TaskDTO> findByUserId(Long userId);
    ResponseEntity<TaskDTO> findById(Long id);
    ResponseEntity<TaskDTO> update(Task task, Long userId);
    ResponseEntity<TaskDTO> updateFinishedStatus(Long id);
    List<TaskDTO> findByFinished(Long userId);
    List<TaskDTO> findByUnfinished(Long userId);
    List<TaskDTO> findByImportant(Long userId);
    ResponseEntity<TaskDTO> deleteById(Long id);
}
