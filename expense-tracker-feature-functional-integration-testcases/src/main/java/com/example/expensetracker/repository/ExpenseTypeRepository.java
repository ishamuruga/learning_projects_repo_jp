package com.example.expensetracker.repository;

import com.example.expensetracker.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {

    Optional<ExpenseType> findByNameIgnoreCase(String name);
}
