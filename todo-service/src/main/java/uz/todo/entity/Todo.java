package uz.todo.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.todo.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        completed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
