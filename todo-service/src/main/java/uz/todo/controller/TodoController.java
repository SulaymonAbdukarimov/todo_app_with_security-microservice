package uz.todo.controller;

import uz.todo.dto.request.TodoRequest;
import uz.todo.dto.response.ApiResponse;
import uz.todo.dto.response.TodoResponse;
import uz.todo.enums.Priority;
import uz.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> create(
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Todo created", todoService.createTodo(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TodoResponse>>> getAll(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<TodoResponse> todos = todoService.getAllTodos(completed, priority, search, page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Todos retrieved", todos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Todo retrieved", todoService.getTodoById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Todo updated", todoService.updateTodo(id, request)));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<TodoResponse>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Todo toggled", todoService.toggleComplete(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok(ApiResponse.success("Todo deleted", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved", todoService.getStats()));
    }
}
