package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummary;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.ExpenseType;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.ExpenseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseTypeRepository expenseTypeRepository;

    public ExpenseResponse addExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        mapRequestToEntity(request, expense);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findByOrderByExpenseDateDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByExpenseType(String expenseType) {
        return expenseRepository.findByExpenseTypeNameIgnoreCase(expenseType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = findById(id);
        mapRequestToEntity(request, expense);
        return toResponse(expenseRepository.save(expense));
    }

    public void deleteExpense(Long id) {
        expenseRepository.delete(findById(id));
    }

    @Transactional(readOnly = true)
    public ExpenseSummary getSummary() {
        ExpenseSummary summary = new ExpenseSummary();
        summary.setTotalCount(expenseRepository.count());
        BigDecimal total = expenseRepository.sumAllAmounts();
        summary.setTotalAmount(total != null ? total : BigDecimal.ZERO);
        summary.setCategories(expenseRepository.findAllCategories());
        return summary;
    }

    private Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    private void mapRequestToEntity(ExpenseRequest request, Expense expense) {
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseType(findOrCreateExpenseType(request.getExpenseType()));
        expense.setExpenseDate(request.getExpenseDate());
    }

    private ExpenseType findOrCreateExpenseType(String expenseTypeName) {
        return expenseTypeRepository.findByNameIgnoreCase(expenseTypeName)
                .orElseGet(() -> expenseTypeRepository.save(new ExpenseType(expenseTypeName)));
    }

    private ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setTitle(expense.getTitle());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setExpenseType(expense.getExpenseType().getName());
        response.setExpenseDate(expense.getExpenseDate());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
