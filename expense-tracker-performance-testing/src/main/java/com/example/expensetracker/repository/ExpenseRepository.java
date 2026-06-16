package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseTypeNameIgnoreCase(String expenseType);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByOrderByExpenseDateDesc();

    @Query("SELECT SUM(e.amount) FROM Expense e")
    BigDecimal sumAllAmounts();

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE LOWER(e.expenseType.name) = LOWER(:expenseType)")
    BigDecimal sumAmountByExpenseType(@Param("expenseType") String expenseType);

    @Query("SELECT DISTINCT e.expenseType.name FROM Expense e ORDER BY e.expenseType.name")
    List<String> findAllCategories();
}
