package com.example.expensetracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.expensetracker.dto.ExpenseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/test-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql/test-seed.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql/test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
class ExpenseControllerActualDataFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Value("${test.sql.input.path:}")
    private String customSqlInputPath;

    @BeforeEach
    void applyCustomSqlIfProvided() {
        if (customSqlInputPath == null || customSqlInputPath.isBlank()) {
            return;
        }

        Resource resource = new ClassPathResource(customSqlInputPath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Custom SQL input file not found on classpath: " + customSqlInputPath);
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.execute(dataSource);
    }

    @Nested
    @DisplayName("GET /api/expenses")
    class GetAllExpensesIntegration {

        @Test
        @DisplayName("getAllExpenses_seededData_returns200")
        void getAllExpenses_seededData_returns200() throws Exception {
            mockMvc.perform(get("/api/expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].title").exists())
                    .andExpect(jsonPath("$[0].expenseType").exists());
        }

        @Test
        @DisplayName("getAllExpenses_byExpenseType_returnsFilteredRows")
        void getAllExpenses_byExpenseType_returnsFilteredRows() throws Exception {
            mockMvc.perform(get("/api/expenses").param("expenseType", "Food"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].expenseType").value("Food"));
        }

        @Test
        @DisplayName("getAllExpenses_byDateRange_returnsExpectedRows")
        void getAllExpenses_byDateRange_returnsExpectedRows() throws Exception {
            mockMvc.perform(get("/api/expenses")
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/{id}")
    class GetByIdIntegration {

        @Test
        @DisplayName("getExpenseById_existingId_returns200")
        void getExpenseById_existingId_returns200() throws Exception {
            mockMvc.perform(get("/api/expenses/1001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1001))
                    .andExpect(jsonPath("$.title").value("Seed Lunch"));
        }

        @Test
        @DisplayName("getExpenseById_missingId_returns404")
        void getExpenseById_missingId_returns404() throws Exception {
            mockMvc.perform(get("/api/expenses/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("POST /api/expenses")
    class CreateExpenseIntegration {

        @Test
        @DisplayName("createExpense_validBody_returns201")
        void createExpense_validBody_returns201() throws Exception {
            ExpenseRequest request = new ExpenseRequest();
            request.setTitle("Integration Created Expense");
            request.setDescription("Created from integration test");
            request.setAmount(new BigDecimal("42.00"));
            request.setExpenseType("Food");
            request.setExpenseDate(LocalDate.of(2026, 6, 10));

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value("Integration Created Expense"));
        }

        @Test
        @DisplayName("createExpense_invalidBody_returns400")
        void createExpense_invalidBody_returns400() throws Exception {
            ExpenseRequest request = new ExpenseRequest();
            request.setTitle("");
            request.setAmount(new BigDecimal("0.00"));
            request.setExpenseType("Food");
            request.setExpenseDate(null);

            mockMvc.perform(post("/api/expenses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    @DisplayName("PUT /api/expenses/{id}")
    class UpdateExpenseIntegration {

        @Test
        @DisplayName("updateExpense_existingId_returns200")
        void updateExpense_existingId_returns200() throws Exception {
            ExpenseRequest request = new ExpenseRequest();
            request.setTitle("Updated Seed Lunch");
            request.setDescription("Updated by integration test");
            request.setAmount(new BigDecimal("25.00"));
            request.setExpenseType("Food");
            request.setExpenseDate(LocalDate.of(2026, 6, 11));

            mockMvc.perform(put("/api/expenses/1001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1001))
                    .andExpect(jsonPath("$.title").value("Updated Seed Lunch"));
        }

        @Test
        @DisplayName("updateExpense_missingId_returns404")
        void updateExpense_missingId_returns404() throws Exception {
            ExpenseRequest request = new ExpenseRequest();
            request.setTitle("Will Fail");
            request.setDescription("No such row");
            request.setAmount(new BigDecimal("10.00"));
            request.setExpenseType("Food");
            request.setExpenseDate(LocalDate.of(2026, 6, 11));

            mockMvc.perform(put("/api/expenses/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/expenses/{id}")
    class DeleteExpenseIntegration {

        @Test
        @DisplayName("deleteExpense_existingId_returns204")
        void deleteExpense_existingId_returns204() throws Exception {
            mockMvc.perform(delete("/api/expenses/1002"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deleteExpense_missingId_returns404")
        void deleteExpense_missingId_returns404() throws Exception {
            mockMvc.perform(delete("/api/expenses/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/expenses/summary")
    class SummaryIntegration {

        @Test
        @DisplayName("getSummary_seededRows_returnsTotals")
        void getSummary_seededRows_returnsTotals() throws Exception {
            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(3))
                    .andExpect(jsonPath("$.totalAmount").value(130.75))
                    .andExpect(jsonPath("$.categories").isArray());
        }
    }
}
