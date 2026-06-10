package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummary;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@ActiveProfiles("test")
class ExpenseControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Nested
    @DisplayName("POST /api/expenses")
    class AddExpenseTests {

        @Test
        @DisplayName("createExpense_validRequest_returns201")
        void createExpense_validRequest_returns201() throws Exception {
            ExpenseRequest request = validRequest();
            ExpenseResponse response = sampleResponse(1L, "Lunch", "Food", LocalDate.of(2026, 6, 10));

            when(expenseService.addExpense(any(ExpenseRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Lunch"))
                    .andExpect(jsonPath("$.expenseType").value("Food"));
        }

        @Test
        @DisplayName("createExpense_blankTitle_returns400")
        void createExpense_blankTitle_returns400() throws Exception {
            ExpenseRequest request = validRequest();
            request.setTitle("  ");

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));
        }

        @Test
        @DisplayName("createExpense_amountBelowMinimum_returns400")
        void createExpense_amountBelowMinimum_returns400() throws Exception {
            ExpenseRequest request = validRequest();
            request.setAmount(new BigDecimal("0.00"));

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.amount").value("Amount must be greater than 0"));
        }
    }

    @Nested
    @DisplayName("GET /api/expenses")
    class GetAllExpensesTests {

        @Test
        @DisplayName("getAllExpenses_withoutFilters_returns200")
        void getAllExpenses_withoutFilters_returns200() throws Exception {
            List<ExpenseResponse> responses = List.of(
                    sampleResponse(2L, "Cab", "Transport", LocalDate.of(2026, 6, 10)),
                    sampleResponse(1L, "Lunch", "Food", LocalDate.of(2026, 6, 9))
            );
            when(expenseService.getAllExpenses()).thenReturn(responses);

            mockMvc.perform(get("/api/expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[1].id").value(1));

            verify(expenseService, times(1)).getAllExpenses();
        }

        @Test
        @DisplayName("getAllExpenses_withExpenseTypeFilter_returnsFilteredList")
        void getAllExpenses_withExpenseTypeFilter_returnsFilteredList() throws Exception {
            List<ExpenseResponse> responses = List.of(sampleResponse(3L, "Dinner", "Food", LocalDate.of(2026, 6, 8)));
            when(expenseService.getExpensesByExpenseType("Food")).thenReturn(responses);

            mockMvc.perform(get("/api/expenses").param("expenseType", "Food"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].expenseType").value("Food"));

            verify(expenseService, times(1)).getExpensesByExpenseType("Food");
        }

        @Test
        @DisplayName("getAllExpenses_withDateRange_returnsFilteredList")
        void getAllExpenses_withDateRange_returnsFilteredList() throws Exception {
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            List<ExpenseResponse> responses = List.of(sampleResponse(4L, "Electricity", "Utilities", LocalDate.of(2026, 6, 7)));
            when(expenseService.getExpensesByDateRange(startDate, endDate)).thenReturn(responses);

            mockMvc.perform(get("/api/expenses")
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].expenseType").value("Utilities"));

            verify(expenseService, times(1)).getExpensesByDateRange(startDate, endDate);
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("getExpenseById_existingId_returns200")
        void getExpenseById_existingId_returns200() throws Exception {
            when(expenseService.getExpenseById(1L)).thenReturn(sampleResponse(1L, "Lunch", "Food", LocalDate.of(2026, 6, 10)));

            mockMvc.perform(get("/api/expenses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Lunch"));
        }

        @Test
        @DisplayName("getExpenseById_notFound_returns404")
        void getExpenseById_notFound_returns404() throws Exception {
            when(expenseService.getExpenseById(99L)).thenThrow(new ResourceNotFoundException("Expense not found with id: 99"));

            mockMvc.perform(get("/api/expenses/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }
    }

    @Nested
    @DisplayName("PUT /api/expenses/{id}")
    class UpdateExpenseTests {

        @Test
        @DisplayName("updateExpense_validRequest_returns200")
        void updateExpense_validRequest_returns200() throws Exception {
            ExpenseRequest request = validRequest();
            request.setTitle("Updated Lunch");
            ExpenseResponse response = sampleResponse(1L, "Updated Lunch", "Food", LocalDate.of(2026, 6, 10));
            when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/expenses/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Lunch"));
        }

        @Test
        @DisplayName("updateExpense_notFound_returns404")
        void updateExpense_notFound_returns404() throws Exception {
            ExpenseRequest request = validRequest();
            when(expenseService.updateExpense(eq(99L), any(ExpenseRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Expense not found with id: 99"));

            mockMvc.perform(put("/api/expenses/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }

        @Test
        @DisplayName("updateExpense_invalidRequest_returns400")
        void updateExpense_invalidRequest_returns400() throws Exception {
            ExpenseRequest request = validRequest();
            request.setExpenseDate(null);

            mockMvc.perform(put("/api/expenses/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.expenseDate").value("Expense date is required"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/expenses/{id}")
    class DeleteExpenseTests {

        @Test
        @DisplayName("deleteExpense_existingId_returns204")
        void deleteExpense_existingId_returns204() throws Exception {
            doNothing().when(expenseService).deleteExpense(1L);

            mockMvc.perform(delete("/api/expenses/1"))
                    .andExpect(status().isNoContent());

            verify(expenseService, times(1)).deleteExpense(1L);
        }

        @Test
        @DisplayName("deleteExpense_notFound_returns404")
        void deleteExpense_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Expense not found with id: 99"))
                    .when(expenseService).deleteExpense(99L);

            mockMvc.perform(delete("/api/expenses/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/summary")
    class SummaryTests {

        @Test
        @DisplayName("getSummary_whenDataExists_returns200")
        void getSummary_whenDataExists_returns200() throws Exception {
            ExpenseSummary summary = new ExpenseSummary();
            summary.setTotalCount(3L);
            summary.setTotalAmount(new BigDecimal("120.50"));
            summary.setCategories(List.of("Food", "Transport"));

            when(expenseService.getSummary()).thenReturn(summary);

            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(3))
                    .andExpect(jsonPath("$.totalAmount").value(120.50))
                    .andExpect(jsonPath("$.categories[0]").value("Food"));
        }

        @Test
        @DisplayName("getSummary_whenNoData_returns200WithZeroValues")
        void getSummary_whenNoData_returns200WithZeroValues() throws Exception {
            ExpenseSummary summary = new ExpenseSummary();
            summary.setTotalCount(0L);
            summary.setTotalAmount(new BigDecimal("0.00"));
            summary.setCategories(List.of());

            when(expenseService.getSummary()).thenReturn(summary);

            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.totalAmount").value(0.00))
                    .andExpect(jsonPath("$.categories").isArray());
        }
    }

    private ExpenseRequest validRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Lunch");
        request.setDescription("Team lunch");
        request.setAmount(new BigDecimal("20.00"));
        request.setExpenseType("Food");
        request.setExpenseDate(LocalDate.of(2026, 6, 10));
        return request;
    }

    private ExpenseResponse sampleResponse(Long id, String title, String expenseType, LocalDate expenseDate) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(id);
        response.setTitle(title);
        response.setDescription("Sample description");
        response.setAmount(new BigDecimal("20.00"));
        response.setExpenseType(expenseType);
        response.setExpenseDate(expenseDate);
        response.setCreatedAt(LocalDateTime.of(2026, 6, 10, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2026, 6, 10, 10, 0));
        return response;
    }
}
