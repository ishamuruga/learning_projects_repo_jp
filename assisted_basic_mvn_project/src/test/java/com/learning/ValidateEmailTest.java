package com.learning;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ValidateEmailTest {

    @Test
    public void shouldAcceptTypicalValidEmailAddress() {
        assertTrue(ValidateEmail.isValidEmail("example@example.com"));
    }

    @Test
    public void shouldAcceptEmailWithPlusAndSubdomain() {
        assertTrue(ValidateEmail.isValidEmail("first.last+tag@mail.example.co"));
    }

    @Test
    public void shouldRejectEmailWithoutAtSymbol() {
        assertFalse(ValidateEmail.isValidEmail("invalid-email"));
    }

    @Test
    public void shouldRejectEmailWithoutTopLevelDomain() {
        assertFalse(ValidateEmail.isValidEmail("example@example"));
    }

    @Test
    public void shouldThrowWhenEmailIsNull() {
        assertThrows(NullPointerException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ValidateEmail.isValidEmail(null);
            }
        });
    }
}