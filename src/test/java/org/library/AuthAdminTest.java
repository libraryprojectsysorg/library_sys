package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthAdminTest {

    @Mock private BorrowService borrowService;
    @Mock private ReminderService reminderService;
    @Mock private FineCalculator fineCalculator;
    @Mock private BookCDService bookCDService;

    private AuthAdmin authAdmin;
    private static final String TEST_USER_FILE = "test_users_auth.txt";

    @BeforeEach
    void setUp() {

        File file = new File(TEST_USER_FILE);
        if (file.exists()) file.delete();

        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_USER_FILE, true))) {
            writer.println("admin@test.com,admin123,SUPER_ADMIN,SA01,Super Admin");
            writer.println("normal@test.com,user123,USER,U01,Normal User");
        } catch (IOException e) {
            fail("فشل في إعداد ملف الاختبار: " + e.getMessage());
        }


        UserFileHandler.setUsersFile(TEST_USER_FILE);


        authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookCDService);
    }

    @AfterEach
    void tearDown() {
        File file = new File(TEST_USER_FILE);
        if (file.exists()) file.delete();
    }

    @Test
    void loginAsSuperAdmin_ShouldSucceed() {
        boolean result = authAdmin.login("admin@test.com", "admin123");

        assertTrue(result, "يجب أن ينجح تسجيل الدخول");
        assertTrue(authAdmin.isSuperAdmin(), "يجب التعرف عليه كـ Super Admin");
        assertTrue(authAdmin.isLoggedInAdmin(), "يجب التعرف عليه كـ Admin");
    }

    @Test
    void loginAsUser_ShouldSucceed() {
        boolean result = authAdmin.login("normal@test.com", "user123");

        assertTrue(result, "يجب أن ينجح تسجيل الدخول");
        assertTrue(authAdmin.isLoggedInUser(), "يجب التعرف عليه كـ User");
        assertFalse(authAdmin.isSuperAdmin(), "لا يجب أن يكون Super Admin");
        assertFalse(authAdmin.isLoggedInAdmin(), "لا يجب أن يكون Admin");
    }

    @Test
    void loginWithWrongPassword_ShouldFail() {
        boolean result = authAdmin.login("admin@test.com", "wrongpass");

        assertFalse(result, "يجب أن يفشل تسجيل الدخول بكلمة مرور خاطئة");
        assertFalse(authAdmin.isSuperAdmin());
        assertFalse(authAdmin.isLoggedInUser());
        assertFalse(authAdmin.isLoggedInAdmin());
    }

    @Test
    void logout_ShouldResetLoginState() {
        authAdmin.login("admin@test.com", "admin123");
        assertTrue(authAdmin.isSuperAdmin());

        authAdmin.logout();

        assertFalse(authAdmin.isSuperAdmin(), "يجب تسجيل الخروج من Super Admin");
        assertFalse(authAdmin.isLoggedInUser(), "يجب تسجيل الخروج من User");
        assertFalse(authAdmin.isLoggedInAdmin(), "يجب تسجيل الخروج من Admin");
    }
    @Test
    void getErrorMessage_WhenNotLoggedIn_ShouldReturnInvalidMessage() {
        assertEquals("Invalid credentials - please try again.", authAdmin.getErrorMessage());
    }

    @Test
    void getErrorMessage_WhenLoggedIn_ShouldReturnSuccessMessage() {
        authAdmin.login("admin@test.com", "admin123");
        assertEquals("Login successful", authAdmin.getErrorMessage());
    }
    @Test
    void findUserById_ShouldReturnCorrectUser() {
        User user = authAdmin.findUserById("SA01");
        assertNotNull(user);
        assertEquals("admin@test.com", user.getEmail());
    }
    @Test
    void addBookInteractive_ShouldCallServiceAndPrintSuccess() {
        Scanner scanner = new Scanner("Test Book\nAuthor A\n12345\n");
        when(bookCDService.addBook("Test Book", "Author A", "12345")).thenReturn(true);

        authAdmin.addBookInteractive(scanner);
    }
    @Test
    void deleteBookInteractive_ShouldCallServiceAndPrintSuccess() {
        Scanner scanner = new Scanner("12345\n");
        when(bookCDService.removeByIsbn("12345")).thenReturn(true);

        authAdmin.deleteBookInteractive(scanner);
    }
    @Test
    void addCDInteractive_ShouldAddCD() {
        Scanner scanner = new Scanner("CD01\nMy CD\nArtist A\n");
        when(CDFileHandler.saveCD(any())).thenReturn(true);

        authAdmin.addCDInteractive(scanner);
    }
    @Test
    void deleteCDInteractive_ShouldDeleteCD() {
        Scanner scanner = new Scanner("CD01\n");
        when(CDFileHandler.removeCDByCode("CD01")).thenReturn(true);

        authAdmin.deleteCDInteractive(scanner);
    }


}
