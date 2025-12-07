package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.Book;
import org.library.Domain.CD;
import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.*;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAdminTest {

    @Mock private BorrowService borrowService;
    @Mock private ReminderService reminderService;
    @Mock private FineCalculator fineCalculator;
    @Mock private BookCDService bookCDService;

    @Mock private Scanner scanner;

    @InjectMocks
    private AuthAdmin authAdmin;

    private static final Path TEST_USER_FILE = Path.of("target", "test-users.txt");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(TEST_USER_FILE.getParent());
        Files.writeString(TEST_USER_FILE, """
        admin@test.com,admin123,SUPER_ADMIN,SA01,Super Admin
        normal@test.com,user123,USER,U02,Normal User
        """, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        UserFileHandler.setUsersFile(TEST_USER_FILE.toString());


        authAdmin.loadUsers();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEST_USER_FILE);
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
        assertFalse(authAdmin.login("admin@test.com", "wrong"));
    }

    @Test
    void logout_ShouldResetState() {
        authAdmin.login("admin@test.com", "admin123");
        authAdmin.logout();
        assertFalse(authAdmin.isLoggedInAdmin());
    }

    @Test
    void addBookInteractive_ShouldCallService() {
        when(scanner.nextLine())
                .thenReturn("كتاب جديد")
                .thenReturn("مؤلف جديد")
                .thenReturn("ISBN123");

        when(bookCDService.addBook(anyString(), anyString(), anyString())).thenReturn(true);

        authAdmin.addBookInteractive(scanner);

        verify(bookCDService).addBook("كتاب جديد", "مؤلف جديد", "ISBN123");
    }

    @Test
    void deleteBookInteractive_ShouldCallService() {
        when(scanner.nextLine()).thenReturn("ISBN123");
        when(bookCDService.removeByIsbn("ISBN123")).thenReturn(true);

        authAdmin.deleteBookInteractive(scanner);

        verify(bookCDService).removeByIsbn("ISBN123");
    }

    @Test
    void addCDInteractive_ShouldCallService() {
        when(scanner.nextLine())
                .thenReturn("كتاب الجافا")     // عنوان
                .thenReturn("أحمد محمد")       // مؤلف
                .thenReturn("ISBN999");         // ISBN

        when(bookCDService.addBook(anyString(), anyString(), anyString())).thenReturn(true);

        authAdmin.addBookInteractive(scanner);

        verify(bookCDService).addBook("كتاب الجافا", "أحمد محمد", "ISBN999");
    }

    @Test
    void deleteCDInteractive_ShouldCallService() {
        when(scanner.nextLine()).thenReturn("CD001");
        when(bookCDService.removeCDByCode("CD001")).thenReturn(true);

        authAdmin.deleteCDInteractive(scanner);

        verify(bookCDService).removeCDByCode("CD001");
    }

    @Test
    void findUserById_ShouldReturnCorrectUser() {
        User user = authAdmin.findUserById("SA01");
        assertNotNull(user);
        assertEquals("admin@test.com", user.getEmail());
        assertEquals("Super Admin", user.getName());
    }
}