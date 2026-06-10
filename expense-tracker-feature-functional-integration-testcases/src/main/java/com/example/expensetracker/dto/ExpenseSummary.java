package com.example.expensetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseSummary {

    private long totalCount;
    private BigDecimal totalAmount;
    private List<String> categories;

}
