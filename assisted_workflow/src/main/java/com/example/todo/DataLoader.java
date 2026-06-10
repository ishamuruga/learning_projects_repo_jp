package com.example.todo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.todo.model.Priority;
import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final TodoRepository todoRepository;

    public DataLoader(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public void run(String... args) {

        todoRepository.deleteAll(); // Clear existing data for a fresh start

        if (todoRepository.count() > 0) {
            return;
        }

        Todo t1 = new Todo("Set up CI/CD pipeline", "Configure GitHub Actions for build and deployment", Priority.HIGH);
        todoRepository.save(t1);

        Todo t2 = new Todo("Write unit tests", "Add test coverage for service layer", Priority.HIGH);
        todoRepository.save(t2);

        Todo t3 = new Todo("Update README", "Document API endpoints and setup instructions", Priority.MEDIUM);
        todoRepository.save(t3);

        Todo t4 = new Todo("Code review backlog", "Review open pull requests from team members", Priority.MEDIUM);
        todoRepository.save(t4);

        Todo t5 = new Todo("Upgrade dependencies", "Bump Spring Boot and library versions", Priority.LOW);
        todoRepository.save(t5);

        Todo t6 = new Todo("Fix login page styling", "Align form fields and improve mobile layout", Priority.LOW);
        todoRepository.save(t6);

        // One closed and one cancelled to demonstrate all statuses
        Todo t7 = new Todo("Initial database schema", "Create tables for users and sessions", Priority.HIGH);
        t7.setStatus(Status.CLOSED);
        t7.setRemarks("Schema created and verified in staging environment");
        todoRepository.save(t7);

        Todo t8 = new Todo("Spike: GraphQL migration", "Evaluate moving REST endpoints to GraphQL", Priority.MEDIUM);
        t8.setStatus(Status.CANCELLED);
        t8.setRemarks("Deprioritised — REST API meets current requirements");
        todoRepository.save(t8);

        Todo t10 = new Todo("My new task007", "Align form fields and improve web angular", Priority.LOW);
        todoRepository.save(t10);

        System.out.println("Sample todo data loaded (" + todoRepository.count() + " records)");
    }
}
