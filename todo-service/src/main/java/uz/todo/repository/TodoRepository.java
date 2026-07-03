package uz.todo.repository;
import uz.todo.entity.Todo;
import uz.todo.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    Page<Todo> findByUserId(Long userId, Pageable pageable);

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    Page<Todo> findByUserIdAndCompleted(Long userId, boolean completed, Pageable pageable);

    Page<Todo> findByUserIdAndPriority(Long userId, Priority priority, Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId " +
            "AND (:completed IS NULL OR t.completed = :completed) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Todo> findByFilters(
            @Param("userId") Long userId,
            @Param("completed") Boolean completed,
            @Param("priority") Priority priority,
            @Param("search") String search,
            Pageable pageable);

    List<Todo> findByUserIdAndDueDateBefore(Long userId, LocalDate date);

    long countByUserIdAndCompleted(Long userId, boolean completed);

    List<Todo> findByCompletedFalseAndDueSoonNotifiedFalseAndDueDateLessThanEqual(LocalDate date);
}
