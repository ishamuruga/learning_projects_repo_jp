package com.learning;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void mainShouldPrintHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(outputStream));

            App.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("Hello World!" + System.lineSeparator(), outputStream.toString());
    }
}
