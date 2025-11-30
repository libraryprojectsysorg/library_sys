package org.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthAdminTest {

    @Mock private BorrowService borrowService;
    @Mock private ReminderService reminderService;
    @Mock private FineCalculator fineCalculator;
    @Mock private BookCDService bookCDService;

    private AuthAdmin authAdmin;
    private static final String TEST_USER_FILE = "test_users_auth.txt";

    @BeforeEach
    void setUp() {

        UserFileHandler UserFileHandler = null;
        UserFileHandler.setUsersFile(TEST_USER_FILE);


        File file = new File(TEST_USER_FILE);
        if (file.exists()) file.delete();


        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_USER_FILE, true))) {

            writer.println("admin@test.com,admin123,SUPER_ADMIN,SA01,Super Admin");
            writer.println("normal@test.com,user123,USER,U01,Normal User");
        } catch (IOException e) {
            fail("فشل في إعداد ملف الاختبار: " + e.getMessage());
        }


        authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookCDService);
    }

    @AfterEach
    void tearDown() {

        File file = new File(TEST_USER_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldLoginSuccessfully_AsSuperAdmin() {

        boolean result = authAdmin.login("admin@test.com", "admin123");

        assertTrue(result, "يجب أن ينجح تسجيل الدخول");

        assertTrue(authAdmin.isSuperAdmin(), "يجب أن يتم التعرف عليه كـ Super Admin");
    }

    @Test
    void shouldLoginSuccessfully_AsUser() {

        boolean result = authAdmin.login("normal@test.com", "user123");


        assertTrue(result, "يجب أن ينجح تسجيل الدخول");
        assertTrue(authAdmin.isLoggedInUser(), "يجب أن يتم التعرف عليه كـ User");
        assertFalse(authAdmin.isLoggedInAdmin());
    }

    @Test
    void shouldFailLogin_WithWrongPassword() {

        boolean result = authAdmin.login("admin@test.com", "wrongpass");


        assertFalse(result, "يجب أن يفشل تسجيل الدخول بكلمة مرور خاطئة");
    }

    @Test
    void shouldLogoutCorrectly() {

        authAdmin.login("admin@test.com", "admin123");
        assertTrue(authAdmin.isSuperAdmin());


        authAdmin.logout();


        assertFalse(authAdmin.isSuperAdmin());
        assertFalse(authAdmin.isLoggedInUser());
    }
}