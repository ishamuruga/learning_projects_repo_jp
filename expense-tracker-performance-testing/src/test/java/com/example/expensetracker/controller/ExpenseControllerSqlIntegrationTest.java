package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/base-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ExpenseControllerSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Happy path scenarios")
    class HappyPathTests {

        @Test
        @DisplayName("createExpense_withExistingType_returns201")
        void createExpense_withExistingType_returns201() throws Exception {
            ExpenseRequest request = validRequest();

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("Team lunch"))
                    .andExpect(jsonPath("$.expenseType").value("Food"));
        }

        @Test
        @DisplayName("createExpense_withNewType_autoCreatesType")
        void createExpense_withNewType_autoCreatesType() throws Exception {
            ExpenseRequest request = validRequest();
            request.setExpenseType("Travel");

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.expenseType").value("Travel"));

            mockMvc.perform(get("/api/expenses").param("expenseType", "travel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].expenseType").value("Travel"));
        }

        @Test
        @DisplayName("getAllExpenses_withoutFilters_returnsOrderedByDateDesc")
        void getAllExpenses_withoutFilters_returnsOrderedByDateDesc() throws Exception {
            mockMvc.perform(get("/api/expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0].title").value("Electricity"))
                    .andExpect(jsonPath("$[1].title").value("Dinner"));
        }

        @Test
        @DisplayName("getExpenseById_existingId_returns200")
        void getExpenseById_existingId_returns200() throws Exception {
            mockMvc.perform(get("/api/expenses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Breakfast"));
        }

        @Test
        @DisplayName("updateExpense_existingId_returns200WithUpdatedFields")
        void updateExpense_existingId_returns200WithUpdatedFields() throws Exception {
            ExpenseRequest updateRequest = validRequest();
            updateRequest.setTitle("Updated Team Lunch");
            updateRequest.setAmount(new BigDecimal("28.75"));

            mockMvc.perform(put("/api/expenses/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Updated Team Lunch"))
                    .andExpect(jsonPath("$.amount").value(28.75));
        }

        @Test
        @DisplayName("deleteExpense_existingId_returns204AndRemovesRecord")
        void deleteExpense_existingId_returns204AndRemovesRecord() throws Exception {
            mockMvc.perform(delete("/api/expenses/2"))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/expenses/2"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 2"));
        }

        @Test
        @DisplayName("getSummary_whenDataExists_returnsAggregates")
        void getSummary_whenDataExists_returnsAggregates() throws Exception {
            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(4))
                    .andExpect(jsonPath("$.totalAmount").value(237.75))
                    .andExpect(jsonPath("$.categories[0]").value("Food"))
                    .andExpect(jsonPath("$.categories[1]").value("Transport"))
                    .andExpect(jsonPath("$.categories[2]").value("Utilities"));
        }
    }

    @Nested
    @DisplayName("Edge scenarios")
    class EdgeTests {

        @Test
        @DisplayName("getExpensesByType_caseInsensitiveFilter_returnsMatches")
        void getExpensesByType_caseInsensitiveFilter_returnsMatches() throws Exception {
            mockMvc.perform(get("/api/expenses").param("expenseType", "fOoD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].expenseType").value("Food"))
                    .andExpect(jsonPath("$[1].expenseType").value("Food"));
        }

        @Test
        @DisplayName("getExpensesByDateRange_inclusiveBoundaries_returnsBoundaryRows")
        void getExpensesByDateRange_inclusiveBoundaries_returnsBoundaryRows() throws Exception {
            mockMvc.perform(get("/api/expenses")
                            .param("startDate", "2026-06-02")
                            .param("endDate", "2026-06-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("getExpenses_withOnlyStartDate_fallsBackToUnfilteredList")
        void getExpenses_withOnlyStartDate_fallsBackToUnfilteredList() throws Exception {
            mockMvc.perform(get("/api/expenses").param("startDate", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4));
        }

        @Test
        @DisplayName("getExpensesByType_whenNoMatch_returnsEmptyList")
        void getExpensesByType_whenNoMatch_returnsEmptyList() throws Exception {
            mockMvc.perform(get("/api/expenses").param("expenseType", "Healthcare"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @DisplayName("getSummary_whenNoData_returnsZeroAndEmptyCategories")
        void getSummary_whenNoData_returnsZeroAndEmptyCategories() throws Exception {
            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.totalAmount").value(0))
                    .andExpect(jsonPath("$.categories.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Exceptional scenarios")
    class ExceptionalTests {

        @Test
        @DisplayName("getExpenseById_notFound_returns404")
        void getExpenseById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/expenses/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }

        @Test
        @DisplayName("updateExpense_notFound_returns404")
        void updateExpense_notFound_returns404() throws Exception {
            mockMvc.perform(put("/api/expenses/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }

        @Test
        @DisplayName("deleteExpense_notFound_returns404")
        void deleteExpense_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/expenses/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
        }

        @Test
        @DisplayName("createExpense_invalidPayload_returns400WithFieldErrors")
        void createExpense_invalidPayload_returns400WithFieldErrors() throws Exception {
            ExpenseRequest invalidRequest = validRequest();
            invalidRequest.setTitle(" ");
            invalidRequest.setAmount(new BigDecimal("0.00"));
            invalidRequest.setExpenseDate(null);

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"))
                    .andExpect(jsonPath("$.fieldErrors.amount").value("Amount must be greater than 0"))
                    .andExpect(jsonPath("$.fieldErrors.expenseDate").value("Expense date is required"));
        }

        @Test
        @DisplayName("updateExpense_invalidPayload_returns400")
        void updateExpense_invalidPayload_returns400() throws Exception {
            ExpenseRequest invalidRequest = validRequest();
            invalidRequest.setExpenseType(" ");

            mockMvc.perform(put("/api/expenses/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.expenseType").value("Expense type is required"));
        }
    }

    private ExpenseRequest validRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Team lunch");
        request.setDescription("Lunch with product team");
        request.setAmount(new BigDecimal("24.50"));
        request.setExpenseType("Food");
        request.setExpenseDate(LocalDate.of(2026, 6, 12));
        return request;
    }
}
