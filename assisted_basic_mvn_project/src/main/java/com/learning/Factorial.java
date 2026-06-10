package com.learning;

/**
 * The Factorial class provides utility methods for calculating factorial values.
 * A factorial of a non-negative integer n is the product of all positive integers
 * less than or equal to n. For example, 5! = 5 × 4 × 3 × 2 × 1 = 120.
 */
public class Factorial {
  
    /**
     * Calculates the factorial of a given non-negative integer.
     *
     * @param n the non-negative integer for which to calculate the factorial
     * @return the factorial of n as a long value
     * @throws IllegalArgumentException if n is negative
     * 
     * @example
     * factorial(5) returns 120
     * factorial(0) returns 1
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Main method that demonstrates the usage of the factorial calculation.
     * Computes and prints the factorial of the number 5.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        int number = 5;
        System.out.println("Factorial of " + number + " is: " + factorial(number));
    }
}
