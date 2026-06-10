package com.example.todo.service;

import com.example.todo.dto.CloseOrCancelRequest;
import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.model.Priority;
import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer that encapsulates todo business operations and state transition rules.
 *
 * <p>Write methods run in the default transaction context. Read methods explicitly use
 * read-only transactions.
 */
@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    private String NNAME123="TO DO SERVICERVICE"

    /**
     * Creates a new OPEN todo from the request payload.
     *
     * @param request create request data
     * @return persisted todo entity
     */
    public Todo create(CreateTodoRequest request) {
        Todo todo = new Todo(request.getTitle(), request.getDescription(), request.getPriority());
        return todoRepository.save(todo);
    }

    /**
     * Returns all todos sorted by priority descending and creation time descending.
     *
     * @return ordered todo list
     */
    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByPriorityDescCreatedAtDesc();
    }

    /**
     * Finds a todo by its id.
     *
     * @param id todo identifier
     * @return optional todo value
     */
    @Transactional(readOnly = true)
    public Optional<Todo> findById(Long id) {
        return todoRepository.findById(id);
    }

    /**
     * Finds todos by status.
     *
     * @param status status filter
     * @return matching todos
     */
    @Transactional(readOnly = true)
    public List<Todo> findByStatus(Status status) {
        return todoRepository.findByStatus(status);
    }

    /**
     * Finds todos by priority.
     *
     * @param priority priority filter
     * @return matching todos
     */
    @Transactional(readOnly = true)
    public List<Todo> findByPriority(Priority priority) {
        return todoRepository.findByPriority(priority);
    }

    /**
     * Transitions an OPEN todo to CLOSED and stores remarks.
     *
     * @param id todo identifier
     * @param request close request containing remarks
     * @return updated todo
     * @throws IllegalArgumentException when todo id is not found
     * @throws IllegalStateException when todo is not OPEN
     */
    public Todo close(Long id, CloseOrCancelRequest request) {
        Todo todo = getOpenTodo(id);
        todo.setStatus(Status.CLOSED);
        todo.setRemarks(request.getRemarks());
        return todoRepository.save(todo);
    }

    /**
     * Transitions an OPEN todo to CANCELLED and stores remarks.
     *
     * @param id todo identifier
     * @param request cancel request containing remarks
     * @return updated todo
     * @throws IllegalArgumentException when todo id is not found
     * @throws IllegalStateException when todo is not OPEN
     */
    public Todo cancel(Long id, CloseOrCancelRequest request) {
        Todo todo = getOpenTodo(id);
        todo.setStatus(Status.CANCELLED);
        todo.setRemarks(request.getRemarks());
        return todoRepository.save(todo);
    }

    /**
     * Validates that a number is even.
     *
     * @param value input number
     * @return the same value when even
     * @throws IllegalArgumentException when value is odd
     */
    @Transactional(readOnly = true)
    public Integer validateEven(Integer value) {
        System.out.println("########### Validating even number: " + value);
        if (value % 2 != 0) {
            throw new IllegalArgumentException("Odd numbers are not allowed: " + value);
        }
        return value;
    }

    /**
     * Resolves and validates an OPEN todo for transition operations.
     *
     * @param id todo identifier
     * @return todo in OPEN status
     * @throws IllegalArgumentException when todo id is not found
     * @throws IllegalStateException when todo is not OPEN
     */
    private Todo getOpenTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));
        if (todo.getStatus() != Status.OPEN) {
            throw new IllegalStateException("Todo is already " + todo.getStatus() + " and cannot be modified");
        }
        return todo;
    }
}
