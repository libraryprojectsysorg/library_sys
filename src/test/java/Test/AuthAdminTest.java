package Test;  // باقة test




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthService (US1.1, US1.2).
 */
public class AuthAdminTest {
    private AuthAdmin authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthAdmin();
    }

    @Test
    public void testLoginSuccess() {
        boolean result = authService.login("ws2022@gmail.com", "ws1234");  // Valid credentials
        assertTrue(result);  // Success
        assertTrue(authService.isLoggedIn());  // Session active
    }

    @Test
    public void testLoginFail() {
        boolean result = authService.login("wrong", "pass");
        assertFalse(result);  // Invalid → error (no exception, just false)
    }

    @Test
    public void testLogout() {
        authService.login("ws2022@gmail.com", "ws1234");  // Login first
        authService.logout();  // Logout
        assertFalse(authService.isLoggedIn());  // Requires re-login
    }
}