package uz.todo.service;

import uz.todo.dto.request.TodoRequest;
import uz.todo.dto.response.TodoResponse;
import uz.todo.entity.Todo;
import uz.todo.enums.Priority;
import uz.todo.exception.ResourceNotFoundException;
import uz.todo.repository.TodoRepository;
import uz.todo.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.common.event.TodoCompletedEvent;
import uz.common.event.TodoCreatedEvent;
import uz.common.messaging.RabbitConstants;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final RabbitTemplate rabbitTemplate;

    private AuthenticatedUser getCurrentUser() {
        return (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Long currentUserId() {
        return getCurrentUser().id();
    }

    @CacheEvict(value = "todos", allEntries = true)
    public TodoResponse createTodo(TodoRequest request) {
        AuthenticatedUser user = getCurrentUser();

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .userId(user.id())
                .build();

        Todo saved = todoRepository.save(todo);

        rabbitTemplate.convertAndSend(RabbitConstants.TODO_EXCHANGE, RabbitConstants.TODO_CREATED_ROUTING_KEY,
                TodoCreatedEvent.builder()
                        .todoId(saved.getId())
                        .userId(saved.getUserId())
                        .title(saved.getTitle())
                        .build());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getAllTodos(Boolean completed, Priority priority,
                                          String search, int page, int size, String sortBy) {
        AuthenticatedUser user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        return todoRepository.findByFilters(user.id(), completed, priority, search, pageable)
                .map(this::mapToResponse);
    }

    // Cached at the single-item level rather than on getAllTodos: a Page<TodoResponse>
    // has no stable Jackson type hint to deserialize back into on a cache hit (it comes
    // back as a LinkedHashMap and blows up at the ResponseEntity write), whereas a plain
    // TodoResponse is a concrete, safely (de)serializable POJO.
    @Cacheable(value = "todos", key = "#root.target.currentUserId() + '-' + #id")
    @Transactional(readOnly = true)
    public TodoResponse getTodoById(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
        return mapToResponse(todo);
    }

    @CacheEvict(value = "todos", allEntries = true)
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

    @CacheEvict(value = "todos", allEntries = true)
    public TodoResponse toggleComplete(Long id) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));

        todo.setCompleted(!todo.isCompleted());
        Todo saved = todoRepository.save(todo);

        if (saved.isCompleted()) {
            rabbitTemplate.convertAndSend(RabbitConstants.TODO_EXCHANGE, RabbitConstants.TODO_COMPLETED_ROUTING_KEY,
                    TodoCompletedEvent.builder()
                            .todoId(saved.getId())
                            .userId(saved.getUserId())
                            .title(saved.getTitle())
                            .build());
        }

        return mapToResponse(saved);
    }

    @CacheEvict(value = "todos", allEntries = true)
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

    @CacheEvict(value = "todos", allEntries = true)
    public TodoResponse addAttachment(Long id, String fileId) {
        AuthenticatedUser user = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUserId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));

        todo.getAttachmentFileIds().add(fileId);
        return mapToResponse(todoRepository.save(todo));
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
                .attachmentFileIds(new ArrayList<>(todo.getAttachmentFileIds()))
                .build();
    }
}
