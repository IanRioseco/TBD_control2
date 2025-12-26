package com.example.TaskManager.Repository;

import com.example.TaskManager.DTO.SectorCountProjection;
import com.example.TaskManager.DTO.UserSectorCountProjection;
import com.example.TaskManager.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser_Id(Long userId);

    // Legacy query methods expected by tests
    @Query("SELECT t FROM Task t WHERE t.user.id = :id ORDER BY t.dueDate ASC")
    List<Task> findAllByUserIdOrderByDateAsc(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.finished = true AND t.user.id = :id ORDER BY t.dueDate ASC")
    List<Task> findAllByFinishedTrueAndUserIdOrderByDateAsc(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.finished = false AND t.user.id = :id ORDER BY t.dueDate ASC")
    List<Task> findAllByFinishedFalseAndUserIdOrderByDateAsc(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.important = true AND t.user.id = :id ORDER BY t.dueDate ASC")
    List<Task> findAllByImportantTrueAndUserIdOrderByDateAsc(@Param("id") Long id);

    // 1. Tareas por sector (Usuario) - Sin cambios, esta ya funcionaba
    @Query(value = """
        SELECT s.id AS "sectorId", s.name AS "sectorName", COUNT(t.id) AS "total"
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        WHERE t.user_id = :userId AND t.finished = true
        GROUP BY s.id, s.name
    """, nativeQuery = true)
    List<SectorCountProjection> countCompletedBySectorForUser(@Param("userId") Long userId);

    // 2 y 6. Tarea más cercana pendiente
    // CAMBIO: Usamos ST_Distance normal para ordenar, es más seguro para Hibernate que el operador <->
    @Query(value = """
        SELECT t.*
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        JOIN db_user u ON t.user_id = u.user_id
        WHERE u.user_id = :userId AND t.finished = false
        ORDER BY ST_Distance(u.location, s.location) ASC
        LIMIT 1
    """, nativeQuery = true)
    Task findNearestPendingTaskForUser(@Param("userId") Long userId);

    // 3 y 8. Top Sector (Radio X)
    // CAMBIO CRUCIAL: Reemplazado ::geography por CAST(... AS geography) para evitar error de Hibernate
    @Query(value = """
        SELECT s.id AS "sectorId", s.name AS "sectorName", COUNT(t.id) AS "total"
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        JOIN db_user u ON t.user_id = u.user_id
        WHERE u.user_id = :userId 
          AND t.finished = true
          AND ST_DWithin(CAST(u.location AS geography), CAST(s.location AS geography), :distanceInMeters)
        GROUP BY s.id, s.name
        ORDER BY "total" DESC
        LIMIT 1
    """, nativeQuery = true)
    SectorCountProjection topCompletedSectorNearUser(@Param("userId") Long userId, @Param("distanceInMeters") double distanceInMeters);

    // 4 y 9. Promedio distancia
    // CAMBIO CRUCIAL: Reemplazado ::geography por CAST(... AS geography)
    @Query(value = """
        SELECT AVG(ST_Distance(CAST(u.location AS geography), CAST(s.location AS geography)))
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        JOIN db_user u ON t.user_id = u.user_id
        WHERE u.user_id = :userId AND t.finished = true
    """, nativeQuery = true)
    Double avgDistanceCompletedTasksForUser(@Param("userId") Long userId);

    // 5. Global Pendientes - Sin cambios
    @Query(value = """
        SELECT s.id AS "sectorId", s.name AS "sectorName", COUNT(t.id) AS "total"
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        WHERE t.finished = false
        GROUP BY s.id, s.name
        ORDER BY "total" DESC
    """, nativeQuery = true)
    List<SectorCountProjection> pendingTasksBySector();

    // 7. Global por Usuario - Sin cambios
    @Query(value = """
        SELECT u.user_id AS "userId", u.username AS "username", s.id AS "sectorId", s.name AS "sectorName", COUNT(t.id) AS "total"
        FROM task t
        JOIN sector s ON t.sector_id = s.id
        JOIN db_user u ON t.user_id = u.user_id
        WHERE t.finished = true
        GROUP BY u.user_id, u.username, s.id, s.name
        ORDER BY u.username, "total" DESC
    """, nativeQuery = true)
    List<UserSectorCountProjection> completedTasksByUserAndSector();
}