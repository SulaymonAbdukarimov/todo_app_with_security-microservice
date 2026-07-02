package uz.todo.service;

import uz.todo.dto.request.TodoRequest;
import uz.todo.dto.response.TodoResponse;
import uz.todo.entity.Todo;
import uz.todo.enums.Priority;
import uz.todo.exception.ResourceNotFoundException;
import uz.todo.repository.TodoRepository;
import uz.todo.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    private AuthenticatedUser getCurrentUser() {
        return (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public TodoResponse createTodo(TodoRequest request) {
        AuthenticatedUser user = getCurrentUser();

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .userId(user.id())
                .build();

        return mapToResponse(todoRepository.save(todo));
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getAllTodos(Boolean completed, Priority priority,
                                          String search, int page, int size, String sortBy) {
        AuthenticatedUser user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        return todoRepository.findByFilters(user.id(), completed, priority, search, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodoById(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
        return mapToResponse(todo);
    }

    public TodoResponse updateTodo(Long id, TodoRequest request) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));

        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());

        return mapToResponse(todoRepository.save(todo));
    }

    public TodoResponse toggleComplete(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));

        todo.setCompleted(!todo.isCompleted());
        return mapToResponse(todoRepository.save(todo));
    }

    public void deleteTodo(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
        todoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        AuthenticatedUser user = getCurrentUser();
        long total = todoRepository.countByUserIdAndCompleted(user.id(), false)
                + todoRepository.countByUserIdAndCompleted(user.id(), true);
        long completed = todoRepository.countByUserIdAndCompleted(user.id(), true);
        long pending = todoRepository.countByUserIdAndCompleted(user.id(), false);

        return Map.of(
                "total", total,
                "completed", completed,
                "pending", pending
        );
    }

    private TodoResponse mapToResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .priority(todo.getPriority())
                .dueDate(todo.getDueDate())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
