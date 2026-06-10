package com.example.expensetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private String expenseType;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
