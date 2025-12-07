package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Test
    void addAdmin_ShouldReturnTrueWhenAdded() {
        // نجبر النظام يفكر إننا super admin
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.SUPER_ADMIN);
        setPrivateField(authAdmin, "isLoggedIn", true);

        boolean result = authAdmin.addAdmin("newadmin@test.com", "pass123", "A001", "New Admin");

        assertTrue(result);
    }

    @Test
    void deleteAdmin_ShouldReturnTrueWhenDeleted() {
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.SUPER_ADMIN);
        setPrivateField(authAdmin, "isLoggedIn", true);

        // نضيف أدمن عشان نحذفه
        authAdmin.addAdmin("todelete@test.com", "pass", "DEL001", "Delete Me");
        authAdmin.loadUsers(); // مهم جدًا

        boolean result = authAdmin.deleteAdmin("DEL001");

        assertTrue(result);
    }

    @Test
    void unregisterUser_ShouldReturnTrue() {
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.SUPER_ADMIN);
        setPrivateField(authAdmin, "isLoggedIn", true);

        // نضيف يوزر عادي
        UserFileHandler.saveUser("user@test.com", "pass", "USER", "U999", "Test User");
        authAdmin.loadUsers();

        boolean result = authAdmin.unregisterUser("U999");

        assertTrue(result);
    }

    @Test
    void getUserTotalFine_ShouldReturnCorrectAmount() {

        UserFileHandler.saveUser("fineuser@test.com", "pass", "USER", "F001", "Fine User");
        authAdmin.loadUsers();

        int fine = authAdmin.getUserTotalFine("F001");

        assertEquals(0, fine);
    }

    @Test
    void payAllUserFines_ShouldClearFines() {
        UserFileHandler.saveUser("payuser@test.com", "pass", "USER", "P001", "Pay User");
        authAdmin.loadUsers();
        User user = authAdmin.findUserById("P001");

        boolean paid = authAdmin.payAllUserFines(user);

        assertFalse(paid);
    }
    @Test
    void authAdminMenu_ShouldNotCrash() {

        setPrivateField(authAdmin, "isLoggedIn", true);
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.SUPER_ADMIN);


        authAdmin.showAdminMenu(new Scanner("16\n")); // نختار Logout عشان يطلع
    }

    @Test
    void borrowAndReturnBook_ShouldWork() {
        setPrivateField(authAdmin, "isLoggedIn", true);
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.ADMIN);


        authAdmin.borrowBookInteractive(new Scanner("Test Book\nSA01\n"));
        authAdmin.returnBookInteractive(new Scanner("Test Book\n"));
    }

    @Test
    void payFine_ShouldRun() {
        setPrivateField(authAdmin, "isLoggedIn", true);
        setPrivateField(authAdmin, "loggedInRole", AuthAdmin.Role.ADMIN);

        authAdmin.payFineForUserInteractive(new Scanner("admin@test.com\ny\n123456789\n"));
    }
    private void setPrivateField(AuthAdmin authAdmin, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = AuthAdmin.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this.authAdmin, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}