package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthAdmin (US1.1, US1.2).
 * Covers login success/failure, logout, and edge cases.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class AuthAdminTest {
    private AuthAdmin authService;
    private static final String VALID_EMAIL = "ws2022@gmail.com";
    private static final String VALID_PASSWORD = "ws1234";

    @BeforeEach
    public void setUp() {
        authService = new AuthAdmin();
    }

    @Test
    public void testLoginSuccess() {
        boolean result = authService.login(VALID_EMAIL, VALID_PASSWORD);  // Valid credentials
        assertTrue(result);  // Success
        assertTrue(authService.isLoggedIn());  // Session active
    }

    @Test
    public void testLoginFail() {
        boolean result = authService.login("wrong", "pass");
        assertFalse(result);  // Invalid → error (no exception, just false)
        assertFalse(authService.isLoggedIn());  // No session
    }

    @Test
    public void testLogout() {
        authService.login(VALID_EMAIL, VALID_PASSWORD);  // Login first
        authService.logout();  // Logout
        assertFalse(authService.isLoggedIn());  // Requires re-login
    }

    @Test
    public void testLoginNullInput() {  // Edge case
        boolean result = authService.login(null, VALID_PASSWORD);
        assertFalse(result);
    }
}