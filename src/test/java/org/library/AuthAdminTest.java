package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @Mock private CDFileHandler cdFileHandler;
    private AuthAdmin authAdmin;
    private static final String TEST_USER_FILE = "test_users_auth.txt";

    @BeforeEach
    void setUp() throws IOException {
        File file = new File(TEST_USER_FILE);
        if (file.exists()) file.delete();

        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_USER_FILE, true))) {
            writer.println("admin@test.com,admin123,SUPER_ADMIN,SA01,Super Admin");
            writer.println("normal@test.com,user123,USER,U01,Normal User");
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
        assertTrue(authAdmin.login("admin@test.com", "admin123"));
        assertTrue(authAdmin.isSuperAdmin());
        assertTrue(authAdmin.isLoggedInAdmin());
    }

    @Test
    void loginAsUser_ShouldSucceed() {
        assertTrue(authAdmin.login("normal@test.com", "user123"));
        assertTrue(authAdmin.isLoggedInUser());
        assertFalse(authAdmin.isSuperAdmin());
    }

    @Test
    void loginWithWrongPassword_ShouldFail() {
        assertFalse(authAdmin.login("admin@test.com", "wrongpass"));
        assertFalse(authAdmin.isSuperAdmin());
        assertFalse(authAdmin.isLoggedInUser());
        assertFalse(authAdmin.isLoggedInAdmin());
    }

    @Test
    void logout_ShouldResetLoginState() {
        authAdmin.login("admin@test.com", "admin123");
        authAdmin.logout();
        assertFalse(authAdmin.isSuperAdmin());
        assertFalse(authAdmin.isLoggedInUser());
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


    @Test
    void addBookInteractive_ShouldCallServiceAndPrintSuccess() {
        Scanner scanner = new Scanner("Test Book\nAuthor A\n12345\n");
        when(bookCDService.addBook("Test Book", "Author A", "12345")).thenReturn(true);

        authAdmin.addBookInteractive(scanner);
        verify(bookCDService).addBook("Test Book", "Author A", "12345");
    }

    @Test
    void deleteBookInteractive_ShouldCallServiceAndPrintSuccess() {
        Scanner scanner = new Scanner("12345\n");
        when(bookCDService.removeByIsbn("12345")).thenReturn(true);

        authAdmin.deleteBookInteractive(scanner);
        verify(bookCDService).removeByIsbn("12345");
    }

    @Test
    void addCDInteractive_ShouldCallServiceAndPrintSuccess() {
        Scanner scanner = new Scanner("My CD\nArtist A\nCD01\n"); // ترتيب: title, author, isbn
        when(bookCDService.addCD("My CD", "Artist A", "CD01")).thenReturn(true);

        authAdmin.addCDInteractive(scanner);

        verify(bookCDService).addCD("My CD", "Artist A", "CD01");
    }

    @Test
    void deleteCDInteractive_ShouldCallCDFileHandlerAndPrintSuccess() {
        Scanner scanner = new Scanner("CD01\n");


        when(bookCDService.removeCDByIsbn("CD01")).thenReturn(true);


        authAdmin.deleteCDInteractive(scanner);


        verify(bookCDService).removeCDByIsbn("CD01");


    }
}
