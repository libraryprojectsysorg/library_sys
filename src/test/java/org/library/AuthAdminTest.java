package org.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.User;
import org.library.Service.Strategy.BookService;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.fines.FineCalculator;
import org.library.Service.Strategy.ReminderService;
import org.library.Service.Strategy.AuthAdmin;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private FineCalculator mockFineCalculator;
    @Mock
    private ReminderService mockReminderService;
    @Mock
    private BookService mockbookService;
    @Mock
    private User mockUser;

    @BeforeEach
    public void setUp() {

        authService = new AuthAdmin(mockBorrowService, mockReminderService, mockFineCalculator, mockbookService);


        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        originalErr = System.err;
        System.setErr(new PrintStream(originalErr));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(System.in);
    }



    @Test
    public void testLoginSuccess() {
        boolean result = authService.login(VALID_EMAIL, VALID_PASSWORD);
        assertTrue(result);
        assertTrue(authService.isLoggedInAdmin());
    }

    @Test
    public void testLoginFail() {
        boolean result = authService.login("wrong", "pass");
        assertFalse(result);
        assertFalse(authService.isLoggedInAdmin());
    }

    @Test
    public void testLogout() {
        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.logout();
        assertFalse(authService.isLoggedInAdmin());
    }

    @Test
    public void testLoginNullInput() {
        boolean result = authService.login(null, VALID_PASSWORD);
        assertFalse(result);
    }



    @Test
    public void testShowAdminMenuSendReminders() {


        String input = "1\n2\n"; // 1 (Send Reminders), 2 (Logout)
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("Reminders sent to overdue users!"));


        verify(mockReminderService).sendReminders();

        verifyNoInteractions(mockFineCalculator);
    }

    @Test
    public void testShowAdminMenuUnregisterSuccess() {
        String userId = "U001";

        // Arrange: (يجب محاكاة دوال BorrowService المستخدمة في منطق Unregister)
        // 1. لا توجد قروض نشطة (للتأكد من أن check hasActiveLoans ينجح)
        when(mockBorrowService.getLoans()).thenReturn(new ArrayList<>());
        // 2. عملية الإزالة تنجح
        when(mockBorrowService.unregisterUser(userId)).thenReturn(true);

        // إدخال: 3 (Unregister), U001 (Demo User), 2 (Logout)
        String input = "3\n" + userId + "\n2\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);

        // Act
        authService.showAdminMenu();

        // Assert
        String output = outputStream.toString().trim();
        assertTrue(output.contains("User unregistered successfully."));

        // التحقق من استدعاء دوال الخدمة
        verify(mockBorrowService, atLeastOnce()).getLoans();
        verify(mockBorrowService).unregisterUser(userId);
    }

    @Test
    public void testShowAdminMenuFineSummary() {
        // اختبار case 4: التحقق من أن AuthAdmin يستدعي mockFineCalculator
        String userId = "U001";

        // Arrange: محاكاة قيمة الغرامة الإجمالية
        // استخدم any(User.class) لأننا لا نستطيع الوصول لـ User object مباشرة
        when(mockFineCalculator.calculateTotalFine(any(User.class))).thenReturn(150);

        // إدخال: 4 (Fine Summary), U001 (Demo User), 2 (Logout)
        String input = "4\n" + userId + "\n2\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);

        // Act
        authService.showAdminMenu();

        // Assert
        String output = outputStream.toString().trim();
        assertNotNull(output);
        assertTrue(output.contains("Fine summary: 150 NIS (accurate across media types)."));

        // التحقق من استدعاء الخدمة المحقونة
        verify(mockFineCalculator).calculateTotalFine(any(User.class));
    }

    // --- Error/Edge Case Tests (معدّلة قليلاً) ---

    @Test
    public void testShowAdminMenuInvalidInput() {
        String input = "a\n2\n"; // إدخال غير رقمي يتبعه logout
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertTrue(output.contains("Invalid input. Please enter a number."));
        assertTrue(output.contains("Logged out successfully."));

        verifyNoInteractions(mockBorrowService, mockFineCalculator, mockReminderService);
    }

    @Test
    public void testShowAdminMenuInvalidOption() {
        String input = "5\n2\n"; // خيار غير موجود يتبعه logout
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.login(VALID_EMAIL, VALID_PASSWORD);
        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        assertTrue(output.contains("Invalid option. Try again."));
        assertTrue(output.contains("Logged out successfully."));

        verifyNoInteractions(mockBorrowService, mockFineCalculator, mockReminderService);
    }

    @Test
    public void testShowAdminMenuNotLoggedIn() {
        String input = "1\n";
        inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        authService.showAdminMenu();

        String output = outputStream.toString().trim();
        // لا يجب أن تظهر قائمة Admin Menu إذا لم يكن مسجل دخوله
        assertFalse(output.contains("=== Admin Menu ==="));
    }
}