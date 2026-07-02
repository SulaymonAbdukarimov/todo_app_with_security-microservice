package uz.todo.dto.request;

import uz.todo.enums.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TodoRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
}
