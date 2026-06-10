package com.example.todo.controller;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.todo.dto.CloseOrCancelRequest;
import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.model.Priority;
import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Todo sampleTodo;

    @BeforeEach
    void setUp() {
        sampleTodo = new Todo("Write unit tests", "Ensure controller coverage", Priority.HIGH);
        sampleTodo.setStatus(Status.OPEN);
        // Simulate JPA-assigned id via reflection helper
        setId(sampleTodo, 1L);
    }

    // -----------------------------------------------------------------------
    // POST /api/todos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/todos - creates todo and returns 201")
    void create_returnsCreatedWithTodo() throws Exception {
        CreateTodoRequest request = buildCreateRequest("Write unit tests", "desc", Priority.HIGH);
        when(todoService.create(any(CreateTodoRequest.class))).thenReturn(sampleTodo);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todo created successfully"))
                .andExpect(jsonPath("$.data.title").value("Write unit tests"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    @DisplayName("POST /api/todos - returns 400 when title is blank")
    void create_blankTitle_returns400() throws Exception {
        CreateTodoRequest request = buildCreateRequest("", "desc", Priority.HIGH);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        System.out.println("Request with blank title: " + objectMapper.writeValueAsString(request));
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("POST /api/todos - returns 400 when priority is null")
    void create_nullPriority_returns400() throws Exception {
        CreateTodoRequest request = buildCreateRequest("Write unit tests", "desc", null);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoService);
    }

    // -----------------------------------------------------------------------
    // GET /api/todos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/todos - returns all todos when no filter")
    void getAll_noFilter_returnsAll() throws Exception {
        when(todoService.findAll()).thenReturn(List.of(sampleTodo));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Write unit tests"));
    }

    @Test
    @DisplayName("GET /api/todos?status=OPEN - filters todos by status")
    void getAll_withStatusFilter_returnsFilteredTodos() throws Exception {
        when(todoService.findByStatus(Status.OPEN)).thenReturn(List.of(sampleTodo));

        mockMvc.perform(get("/api/todos").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));

        verify(todoService).findByStatus(Status.OPEN);
        verify(todoService, never()).findByPriority(any());
        verify(todoService, never()).findAll();
    }

    @Test
    @DisplayName("GET /api/todos?priority=HIGH - filters todos by priority")
    void getAll_withPriorityFilter_returnsFilteredTodos() throws Exception {
        when(todoService.findByPriority(Priority.HIGH)).thenReturn(List.of(sampleTodo));

        mockMvc.perform(get("/api/todos").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].priority").value("HIGH"));

        verify(todoService).findByPriority(Priority.HIGH);
        verify(todoService, never()).findByStatus(any());
        verify(todoService, never()).findAll();
    }

    @Test
    @DisplayName("GET /api/todos - returns empty list when no todos exist")
    void getAll_empty_returnsEmptyList() throws Exception {
        when(todoService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // -----------------------------------------------------------------------
    // GET /api/todos/{id}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/todos/{id} - returns todo when found")
    void getById_found_returns200() throws Exception {
        when(todoService.findById(1L)).thenReturn(Optional.of(sampleTodo));

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todo found"))
                .andExpect(jsonPath("$.data.title").value("Write unit tests"));
    }

    @Test
    @DisplayName("GET /api/todos/{id} - returns 404 when not found")
    void getById_notFound_returns404() throws Exception {
        when(todoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/todos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 99"));
    }

    @Test
    @DisplayName("GET /api/todos/validate-event?value=8 - returns 200 for even value")
    void validateEven_evenValue_returns200() throws Exception {
        when(todoService.validateEven(8)).thenReturn(8);

        mockMvc.perform(get("/api/todos/validate-event").param("value", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Even number accepted"))
                .andExpect(jsonPath("$.data").value(8));

        System.out.println("#############################################5");
        System.out.println("Performing request without 'value' parameter - 8");
        System.out.println("#############################################6");

        verify(todoService).validateEven(8);
    }

    @Test
    @DisplayName("GET /api/todos/validate-event?value=7 - returns 400 for odd value")
    void validateEven_oddValue_returns400() throws Exception {
        when(todoService.validateEven(7)).thenThrow(new IllegalArgumentException("Odd numbers are not allowed: 7"));

        mockMvc.perform(get("/api/todos/validate-event").param("value", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Odd numbers are not allowed: 7"));

        System.out.println("#############################################3");
        System.out.println("Performing request without 'value' parameter - 7");
        System.out.println("#############################################4");
        verify(todoService).validateEven(7);
    }

    

    @Test
    @DisplayName("GET /api/todos/validate-event - returns 400 when value param is missing")
    void validateEven_missingValue_returns400() throws Exception {
        System.out.println("#############################################1");
        System.out.println("Performing request without 'value' parameter");
        System.out.println("#############################################2");
        mockMvc.perform(get("/api/todos/validate-event"))
                .andExpect(status().isBadRequest());

        verify(todoService, never()).validateEven(any());
    }

    // -----------------------------------------------------------------------
    // PATCH /api/todos/{id}/close
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/todos/{id}/close - closes todo and returns 200")
    void close_success_returns200() throws Exception {
        sampleTodo.setStatus(Status.CLOSED);
        sampleTodo.setRemarks("Done");
        when(todoService.close(eq(1L), any(CloseOrCancelRequest.class))).thenReturn(sampleTodo);

        CloseOrCancelRequest request = buildCloseOrCancelRequest("Done");

        mockMvc.perform(patch("/api/todos/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todo closed successfully"))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/close - returns 404 when todo not found")
    void close_todoNotFound_returns404() throws Exception {
        when(todoService.close(eq(99L), any(CloseOrCancelRequest.class)))
                .thenThrow(new IllegalArgumentException("Todo not found with id: 99"));

        CloseOrCancelRequest request = buildCloseOrCancelRequest("Done");

        mockMvc.perform(patch("/api/todos/99/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 99"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/close - returns 409 when state transition is invalid")
    void close_invalidStateTransition_returns409() throws Exception {
        when(todoService.close(eq(1L), any(CloseOrCancelRequest.class)))
                .thenThrow(new IllegalStateException("Cannot close a todo that is already CLOSED"));

        CloseOrCancelRequest request = buildCloseOrCancelRequest("Done");

        mockMvc.perform(patch("/api/todos/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot close a todo that is already CLOSED"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/close - returns 400 when remarks are blank")
    void close_blankRemarks_returns400() throws Exception {
        CloseOrCancelRequest request = buildCloseOrCancelRequest("");

        mockMvc.perform(patch("/api/todos/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoService);
    }

    // -----------------------------------------------------------------------
    // PATCH /api/todos/{id}/cancel
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/todos/{id}/cancel - cancels todo and returns 200")
    void cancel_success_returns200() throws Exception {
        sampleTodo.setStatus(Status.CANCELLED);
        sampleTodo.setRemarks("No longer needed");
        when(todoService.cancel(eq(1L), any(CloseOrCancelRequest.class))).thenReturn(sampleTodo);

        CloseOrCancelRequest request = buildCloseOrCancelRequest("No longer needed");

        mockMvc.perform(patch("/api/todos/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Todo cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/cancel - returns 404 when todo not found")
    void cancel_todoNotFound_returns404() throws Exception {
        when(todoService.cancel(eq(99L), any(CloseOrCancelRequest.class)))
                .thenThrow(new IllegalArgumentException("Todo not found with id: 99"));

        CloseOrCancelRequest request = buildCloseOrCancelRequest("No longer needed");

        mockMvc.perform(patch("/api/todos/99/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 99"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/cancel - returns 409 when state transition is invalid")
    void cancel_invalidStateTransition_returns409() throws Exception {
        when(todoService.cancel(eq(1L), any(CloseOrCancelRequest.class)))
                .thenThrow(new IllegalStateException("Cannot cancel a todo that is already CANCELLED"));

        CloseOrCancelRequest request = buildCloseOrCancelRequest("Duplicate");

        mockMvc.perform(patch("/api/todos/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot cancel a todo that is already CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/todos/{id}/cancel - returns 400 when remarks are blank")
    void cancel_blankRemarks_returns400() throws Exception {
        CloseOrCancelRequest request = buildCloseOrCancelRequest("   ");

        System.out.println("Request with blank remarks: " + objectMapper.writeValueAsString(request));
        
        mockMvc.perform(patch("/api/todos/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoService);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CreateTodoRequest buildCreateRequest(String title, String description, Priority priority) {
        CreateTodoRequest r = new CreateTodoRequest();
        r.setTitle(title);
        r.setDescription(description);
        r.setPriority(priority);
        return r;
    }

    private CloseOrCancelRequest buildCloseOrCancelRequest(String remarks) {
        CloseOrCancelRequest r = new CloseOrCancelRequest();
        r.setRemarks(remarks);
        return r;
    }

    /** Assigns an id to a Todo without a public setter (id is auto-generated by JPA). */
    private void setId(Todo todo, Long id) {
        try {
            var field = Todo.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(todo, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set Todo id in test setup", e);
        }
    }
}
