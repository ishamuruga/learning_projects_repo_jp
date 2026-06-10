package com.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class FactorialTest {

    @Test
    public void factorialOfZeroShouldBeOne() {
        assertEquals(1L, Factorial.factorial(0));
    }

    @Test
    public void factorialOfOneShouldBeOne() {
        assertEquals(1L, Factorial.factorial(1));
    }

    @Test
    public void factorialOfTwoShouldBeTwo() {
        assertEquals(2L, Factorial.factorial(2));
    }

    @Test
    public void factorialOfFiveShouldBeOneHundredTwenty() {
        assertEquals(120L, Factorial.factorial(5));
    }

    @Test
    public void factorialOfTenShouldBeThreeMillionSixHundredTwentyEightThousandEightHundred() {
        assertEquals(3628800L, Factorial.factorial(10));
    }

    @Test
    public void factorialOfTwentyShouldMatchLongBoundaryValue() {
        assertEquals(2432902008176640000L, Factorial.factorial(20));
    }

    @Test
    public void factorialOfTwentyOneShouldOverflowLongAndWrap() {
        long twentyFactorial = Factorial.factorial(20);
        long twentyOneFactorial = Factorial.factorial(21);

        assertTrue(twentyOneFactorial < twentyFactorial);
        assertTrue(twentyOneFactorial < 0L);
    }

    @Test
    public void factorialOfNegativeNumberShouldThrowException() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() {
                Factorial.factorial(-1);
            }
        });
    }

    @Test
    public void factorialOfIntegerMinValueShouldThrowException() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() {
                Factorial.factorial(Integer.MIN_VALUE);
            }
        });
    }

    @Test
    public void factorialNullHandlingShouldRejectNullWhenInvokedReflectively() throws Exception {
        final Method method = Factorial.class.getDeclaredMethod("factorial", int.class);

        assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() throws Throwable {
                method.invoke(null, new Object[] { null });
            }
        });
    }
}
