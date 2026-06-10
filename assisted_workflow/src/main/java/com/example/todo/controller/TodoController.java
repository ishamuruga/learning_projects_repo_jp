package com.example.todo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.ApiResponse;
import com.example.todo.dto.CloseOrCancelRequest;
import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.model.Priority;
import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

/**
 * REST controller for todo lifecycle APIs under {@code /api/todos}.
 *
 * <p>This layer handles request validation, status code mapping, and response envelope shaping.
 * Business rules and persistence concerns are delegated to {@link TodoService}.
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Creates a new todo in OPEN status.
     *
     * @param request create payload with title, optional description, and priority
     * @return {@code 201 Created} with {@code ApiResponse<Todo>} when successful
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> create(@Valid @RequestBody CreateTodoRequest request) {
        Todo todo = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Todo created successfully", todo));
    }

    /**
     * Lists todos with optional filtering.
     *
     * <p>Filter precedence is status first, then priority, else all todos.
     *
     * @param status optional status filter
     * @param priority optional priority filter
     * @return {@code 200 OK} with {@code ApiResponse<List<Todo>>}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> getAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {

        List<Todo> todos;
        if (status != null) {
            todos = todoService.findByStatus(status);
        } else if (priority != null) {
            todos = todoService.findByPriority(priority);
        } else {
            todos = todoService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.ok("Todos retrieved successfully", todos));
    }

    /**
     * Retrieves a todo by id.
     *
     * @param id todo identifier
     * @return {@code 200 OK} with todo when found, else {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getById(@PathVariable Long id) {
        return todoService.findById(id)
                .map(todo -> ResponseEntity.ok(ApiResponse.ok("Todo found", todo)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Todo not found with id: " + id)));
    }

    /**
     * Closes an OPEN todo with mandatory remarks.
     *
     * @param id todo identifier
     * @param request payload containing closure remarks
     * @return {@code 200 OK} when closed, {@code 404 Not Found} when id is missing,
     *         or {@code 409 Conflict} when todo is not OPEN
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Todo>> close(
            @PathVariable Long id,
            @Valid @RequestBody CloseOrCancelRequest request) {
        try {
            Todo todo = todoService.close(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Todo closed successfully", todo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancels an OPEN todo with mandatory remarks.
     *
     * @param id todo identifier
     * @param request payload containing cancellation remarks
     * @return {@code 200 OK} when cancelled, {@code 404 Not Found} when id is missing,
     *         or {@code 409 Conflict} when todo is not OPEN
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Todo>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CloseOrCancelRequest request) {
        try {
            Todo todo = todoService.cancel(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Todo cancelled successfully", todo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Validates that the provided value is even.
     *
     * @param value number to validate
     * @return {@code 200 OK} with accepted value when even
     * @throws IllegalArgumentException when value is odd
     */
    @GetMapping("/validate-event")
    public ResponseEntity<ApiResponse<Integer>> validateEven(@RequestParam Integer value) {
        Integer accepted = todoService.validateEven(value);
        return ResponseEntity.ok(ApiResponse.ok("Even number accepted", accepted));
    }


    public ResponseEntity<ApiResponse<List<Todo>>> doRegisterVCAMRequest() {
        List<Todo> todos = todoService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Todos retrieved successfully", todos));
    }
}
