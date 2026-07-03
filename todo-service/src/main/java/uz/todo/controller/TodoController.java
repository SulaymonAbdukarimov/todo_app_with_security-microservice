package uz.todo.controller;

import uz.todo.dto.request.TodoRequest;
import uz.todo.dto.response.ApiResponse;
import uz.todo.dto.response.FileUploadResponse;
import uz.todo.dto.response.TodoResponse;
import uz.todo.enums.Priority;
import uz.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uz.common.enums.FileCategory;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final RestTemplate restTemplate;

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

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TodoResponse>> addAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        // Ownership check happens implicitly: getTodoById throws ResourceNotFoundException
        // if the todo doesn't belong to the caller, before we ever call file-service.
        todoService.getTodoById(id);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("category", FileCategory.TODO_ATTACHMENT.name());
        body.add("linkedId", id.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiResponse<FileUploadResponse>> response = restTemplate.exchange(
                "http://FILE-SERVICE/api/v1/files",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });

        FileUploadResponse fileResponse = response.getBody().getData();
        TodoResponse updated = todoService.addAttachment(id, fileResponse.getId().toString());

        return ResponseEntity.ok(ApiResponse.success("Attachment added", updated));
    }
}
