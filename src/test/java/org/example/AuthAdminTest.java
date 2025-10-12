package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthAdminTest {
    private AuthAdmin authService;
    private static final String VALID_EMAIL = "s12217663@stu.najah.edu";
    private static final String VALID_PASSWORD = "ws1234";
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;
    private PrintStream originalErr;

    @Mock
    private BorrowService mockBorrowService;

    @Mock
    private User mockUser;

    @BeforeEach
    public void setUp() {
        authService = new AuthAdmin();
        authService.borrowService = mockBorrowService;

        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        originalErr = System.err;
        System.setErr(new PrintStream(originalErr));
    }

    @Test
    public void testLoginSuccess() {
        boolean result = authService.login(VALID_EMAIL, VALID_PASSWORD);
        assertTrue(result);
        assertTrue(authService.isLoggedIn());
    }

    @Test
    public void testLoginFail() {
        boolean result = authService.login("wrong", "pass");
        assertFalse(result);
        assertFalse(authService.isLoggedIn());
    }

    @Test
    public void testLogout() {
        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.logout();
        assertFalse(authService.isLoggedIn());
    }

    @Test
    public void testLoginNullInput() {
        boolean result = authService.login(null, VALID_PASSWORD);
        assertFalse(result);
    }

    @Test
    public void testShowAdminMenuSendReminders() {
        when(mockBorrowService.getUsersWithOverdueLoans()).thenReturn(List.of(mockUser));
        when(mockBorrowService.countOverdueLoansForUser(mockUser)).thenReturn(2);
        when(mockUser.getEmail()).thenReturn("test@example.com");

        String input = "1\n2\n\n\n\n\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("=== Admin Menu ==="));
        assertTrue(output.contains("Send Overdue Reminders "));
        assertTrue(output.contains("Reminders sent to overdue users!"));
        assertTrue(output.contains("Sent to test@example.com: You have 2 overdue book(s)."));
        assertTrue(output.contains("Logged out successfully."));

        verify(mockBorrowService).getUsersWithOverdueLoans();
        verify(mockBorrowService).countOverdueLoansForUser(mockUser);
    }

    @Test
    public void testShowAdminMenuInvalidInput() {
        String input = "a\n2\n\n\n\n\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("=== Admin Menu ==="));
        assertTrue(output.contains("Invalid input. Please enter a number."));
        assertTrue(output.contains("Logged out successfully."));
        assertFalse(output.contains("Reminders sent"));

        verifyNoInteractions(mockBorrowService);
    }

    @Test
    public void testShowAdminMenuInvalidOption() {
        String input = "5\n2\n\n\n\n\n\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("Invalid option. Try again."));
        assertTrue(output.contains("Logged out successfully."));
        assertFalse(output.contains("Reminders sent"));

        verifyNoInteractions(mockBorrowService);
    }

    @Test
    public void testShowAdminMenuNotLoggedIn() {
        String input = "1\n\n\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertFalse(output.contains("=== Admin Menu ==="));
        assertFalse(output.contains("Reminders sent"));

        verifyNoInteractions(mockBorrowService);
    }

    @Test
    void testShowAdminMenuUnregister() {
        // Arrange: stubb getLoans لتجنب null
        List<Loan> emptyLoans = new ArrayList<>();
        when(mockBorrowService.getLoans()).thenReturn(emptyLoans);

        // lenient stubbing لـ unregisterUser
        lenient().when(mockBorrowService.unregisterUser(anyString())).thenReturn(true);

        // استخدم reflection لتعيين hasUnpaidFines = false للـ demo user (U001)
        try {
            Field usersField = AuthAdmin.class.getDeclaredField("users");
            usersField.setAccessible(true);
            List<User> users = (List<User>) usersField.get(authService);
            User demoUser = users.get(0);  // U001
            Field finesField = User.class.getDeclaredField("hasUnpaidFines");
            finesField.setAccessible(true);
            finesField.set(demoUser, false);  // تعيين false لتمرير الشرط
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        // إصلاح الإدخال: أزل \n الإضافي بعد 3، عشان nextLine() يقرأ U001
        String input = "3\nU001\n2\n\n\n\n\n\n";  // 3, U001, 2, ثم \n إضافي للحلقة
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);

        // Act
        authService.showAdminMenu();

        // Assert
        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("=== Admin Menu ==="));
        assertTrue(output.contains("User unregistered successfully."));  // الآن بتنجح
        assertTrue(output.contains("Logged out successfully."));
        assertFalse(output.contains("Error unregistering user"));
        assertFalse(output.contains("User not found."));  // التأكد من عدم الخروج المبكر
        assertFalse(output.contains("Users with active loans or unpaid fines cannot be unregistered."));

        verify(mockBorrowService, atLeastOnce()).getLoans();
        verify(mockBorrowService).unregisterUser("U001");  // بتنجح الآن
        verifyNoMoreInteractions(mockBorrowService);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(System.in);
    }
}