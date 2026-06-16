package com.example.expensetracker.controller;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummary;
import com.example.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Operations for managing expense records")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
        @Operation(summary = "Create expense", description = "Create a new expense record")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created",
                content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<ExpenseResponse> addExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(request));
    }

    @GetMapping
        @Operation(
            summary = "List expenses",
            description = "Get all expenses, or filter by expenseType, or filter by inclusive startDate/endDate"
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExpenseResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter format",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @Parameter(description = "Expense category/type filter (case-insensitive)", example = "Food")
            @RequestParam(required = false) String expenseType,
            @Parameter(description = "Start date (ISO-8601), used with endDate", example = "2026-06-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO-8601), used with startDate", example = "2026-06-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (expenseType != null) {
            return ResponseEntity.ok(expenseService.getExpensesByExpenseType(expenseType));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
        @Operation(summary = "Get expense by ID", description = "Retrieve a single expense by its identifier")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense found",
                content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping("/{id}")
        @Operation(summary = "Update expense", description = "Replace an existing expense with new values")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated",
                content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<ExpenseResponse> updateExpense(
            @Parameter(description = "Expense ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
        @Operation(summary = "Delete expense", description = "Delete an expense by identifier")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted"),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        String temp = "";
        if (id > 100){
            temp = "hello the value is more than 100";
        }
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
        @Operation(summary = "Get expense summary", description = "Get total count, total amount, and category list")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved",
                content = @Content(schema = @Schema(implementation = ExpenseSummary.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                content = @Content(mediaType = "application/json"))
        })
    public ResponseEntity<ExpenseSummary> getSummary() {
        return ResponseEntity.ok(expenseService.getSummary());
    }
}
