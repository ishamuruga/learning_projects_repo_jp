package com.example.expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.ExpenseType;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.ExpenseTypeRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ExpenseTypeRepository expenseTypeRepository;
    private final ExpenseRepository expenseRepository;
    private final boolean seedEnabled;

    public DataInitializer(ExpenseTypeRepository expenseTypeRepository,
                           ExpenseRepository expenseRepository,
                           @Value("${app.data.seed.enabled:true}") boolean seedEnabled) {
        this.expenseTypeRepository = expenseTypeRepository;
        this.expenseRepository = expenseRepository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        if (expenseTypeRepository.count() > 0) {
            return; // data already seeded
        }

        // Seed expense types
        ExpenseType food        = expenseTypeRepository.save(new ExpenseType("Food"));
        ExpenseType transport   = expenseTypeRepository.save(new ExpenseType("Transport"));
        ExpenseType utilities   = expenseTypeRepository.save(new ExpenseType("Utilities"));
        ExpenseType entertainment = expenseTypeRepository.save(new ExpenseType("Entertainment"));
        ExpenseType healthcare  = expenseTypeRepository.save(new ExpenseType("Healthcare"));

        // Seed sample expenses
        expenseRepository.saveAll(List.of(
            expense("Grocery shopping",    "Weekly groceries from supermarket",  "45.75",  food,          LocalDate.now().minusDays(1)),
            expense("Restaurant dinner",   "Dinner with family at Italian place", "85.00",  food,          LocalDate.now().minusDays(3)),
            expense("Bus pass",            "Monthly public transport pass",       "60.00",  transport,     LocalDate.now().minusDays(5)),
            expense("Uber ride",           "Cab to airport",                      "22.50",  transport,     LocalDate.now().minusDays(7)),
            expense("Electricity bill",    "Monthly electricity charges",         "120.00", utilities,     LocalDate.now().minusDays(10)),
            expense("Internet bill",       "Monthly broadband subscription",      "55.00",  utilities,     LocalDate.now().minusDays(12)),
            expense("Netflix subscription","Monthly streaming subscription",      "15.99",  entertainment, LocalDate.now().minusDays(14)),
            expense("Movie tickets",       "Weekend movie for two",               "30.00",  entertainment, LocalDate.now().minusDays(16)),
            expense("Doctor consultation", "General physician visit",             "75.00",  healthcare,    LocalDate.now().minusDays(20)),
            expense("Pharmacy",            "Monthly prescribed medicines",        "40.00",  healthcare,    LocalDate.now().minusDays(22))
        ));

        System.out.println("Sample expense data loaded successfully.");
    }

    private Expense expense(String title, String description, String amount,
                            ExpenseType type, LocalDate date) {
        Expense e = new Expense();
        e.setTitle(title);
        e.setDescription(description);
        e.setAmount(new BigDecimal(amount));
        e.setExpenseType(type);
        e.setExpenseDate(date);
        return e;
    }
}
