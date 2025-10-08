package org.example;

import java.util.ArrayList;
import java.util.List;


public class AuthAdmin {
    private List<Admin> admins = new ArrayList<>();
    private boolean isLoggedIn = false;


    public AuthAdmin() {
        admins.add(new Admin("ws2022@gmail.com", "ws1234"));
    }


    public boolean login(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            return false;  // Edge case: invalid input (covers null/empty branches)
        }
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(password)) {
                isLoggedIn = true;
                return true;  // Valid → login success
            }
        }
        return false;  // No match → error
    }


    public String getErrorMessage() {  // New method for "error message" acceptance
        if (!isLoggedIn) {
            return "Invalid credentials - please try again.";  // Branch for error
        }
        return "Login successful";  // Branch for success
    }


    public void logout() {
        isLoggedIn = false;
    }


    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}