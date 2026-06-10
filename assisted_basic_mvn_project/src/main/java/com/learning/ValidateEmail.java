package com.learning;

public class ValidateEmail {
    

    public static boolean isValidEmail(String email) {
        // Regular expression for validating an email address
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        
        // Check if the email matches the regex pattern
        return email.matches(emailRegex);
    }

    public static void main(String[] args) {
        String email1 = "example@example.com";
        String email2 = "invalid-email";

        System.out.println(email1 + " is valid: " + isValidEmail(email1));
        System.out.println(email2 + " is valid: " + isValidEmail(email2));
    }

}
