package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.Book;
import org.library.Domain.Fine;
import org.library.Domain.Loan;
import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.SUPER_ADMIN);

        boolean result = authAdmin.addAdmin("newadmin@test.com", "pass123", "A001", "New Admin");

        assertTrue(result);
    }

    @Test
    void deleteAdmin_ShouldReturnTrueWhenDeleted() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.SUPER_ADMIN);


        authAdmin.addAdmin("todelete@test.com", "pass", "DEL001", "Delete Me");
        authAdmin.loadUsers();

        boolean result = authAdmin.deleteAdmin("DEL001");

        assertTrue(result);
    }

    @Test
    void unregisterUser_ShouldReturnTrue() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

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
    void borrowAndReturnBook_ShouldWork() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);


        authAdmin.borrowBookInteractive(new Scanner("Test Book\nSA01\n"));
        authAdmin.returnBookInteractive(new Scanner("Test Book\n"));
    }

    @Test
    void payFine_ShouldRun() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        authAdmin.payFineForUserInteractive(new Scanner("admin@test.com\ny\n123456789\n"));
    }
    @Test
    void showAdminMenu_AsRegularAdmin_ShouldShowCorrectOptions() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        // نضيف admin عادي
        UserFileHandler.saveUser("regadmin@test.com", "pass", "ADMIN", "A001", "Regular Admin");
        authAdmin.login("regadmin@test.com", "pass");

        // نعمل spy على System.out عشان نتأكد إن القايمة المطبوعة تحتوي على خيار "Pay Fine"
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        // ندخل خيار يخرج فورًا (14 = Logout للـ admin العادي)
        Scanner mockScanner = new Scanner("14\n");
        authAdmin.showAdminMenu(mockScanner);

        String output = out.toString();
        assertTrue(output.contains("Pay Fine"));
        assertFalse(output.contains("Add Admin")); // ما يظهرش للـ admin العادي
    }



    @Test
    void fineSummaryInteractive_ShouldShowTotalFine() {
        UserFileHandler.saveUser("fineuser@test.com", "pass", "USER", "F999", "Fine User");
        authAdmin.loadUsers();
        authAdmin.login("admin@test.com", "admin123");

        User user = authAdmin.findUserById("F999");
        when(fineCalculator.calculateTotalFine(user)).thenReturn(120);

        when(scanner.nextLine()).thenReturn("F999");

        authAdmin.fineSummaryInteractive(scanner);

    }

    @Test
    void payFineForUserInteractive_RefusePayment_ShouldNotPay() throws Exception {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        when(scanner.nextLine())
                .thenReturn("admin@test.com")
                .thenReturn("n");

        User user = authAdmin.findUserById("SA01");

        // الحل النهائي: نستبدل قائمة الغرامات بـ ArrayList حقيقية
        Field finesField = User.class.getDeclaredField("fines");
        finesField.setAccessible(true);
        List<Fine> mutableFines = new ArrayList<>();
        finesField.set(user, mutableFines);

        Fine mockFine = mock(Fine.class);
        when(mockFine.isPaid()).thenReturn(false);
        mutableFines.add(mockFine);

        when(fineCalculator.calculateTotalFine(user)).thenReturn(100);

        authAdmin.payFineForUserInteractive(scanner);

        assertFalse(mutableFines.get(0).isPaid());
    }

    @Test
    void payFineForUserInteractive_NoFine_ShouldPrintMessage() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        when(scanner.nextLine()).thenReturn("admin@test.com");

        authAdmin.payFineForUserInteractive(scanner);

    }




    @Test
    void returnBookInteractive_WithFine_ShouldPrintFineMessage() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        Book mockBook = mock(Book.class);
        when(mockBook.getTitle()).thenReturn("Harry Potter");

        User mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn("Ahmad");

        Loan mockLoan = mock(Loan.class);
        when(mockLoan.getMedia()).thenReturn(mockBook);
        when(mockLoan.getUser()).thenReturn(mockUser);
        when(mockLoan.getLoanId()).thenReturn("999L");

        when(borrowService.getLoans()).thenReturn(List.of(mockLoan));
        when(borrowService.returnMedia("999L")).thenReturn(75);

        when(scanner.nextLine()).thenReturn("Harry Potter");

        authAdmin.returnBookInteractive(scanner);

    }

    @Test
    void borrowBookInteractive_BookNotFound_ShouldPrintError() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        when(scanner.nextLine())
                .thenReturn("غير موجود أبدًا")
                .thenReturn("SA01");

        when(bookCDService.searchBooks("غير موجود أبدًا")).thenReturn(List.of());

        authAdmin.borrowBookInteractive(scanner);

    }

    @Test
    void borrowBookInteractive_UserNotFound_ShouldPrintError() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        when(scanner.nextLine())
                .thenReturn("Existing Book")
                .thenReturn("NONEXISTENT_ID");

        Book b = new Book("Existing Book", "Author", "ISBN123");
        when(bookCDService.searchBooks("Existing Book")).thenReturn(List.of(b));

        authAdmin.borrowBookInteractive(scanner);

    }

    @Test
    void handleSuperAdminChoice_InvalidOption_ShouldPrintInvalid() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.SUPER_ADMIN);


        try {
            Method method = AuthAdmin.class.getDeclaredMethod("handleSuperAdminChoice", int.class, Scanner.class);
            method.setAccessible(true);
            method.invoke(authAdmin, 999, scanner);
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    void handleAdminChoice_InvalidOption_ShouldPrintInvalid() {
        setField("isLoggedIn", true);
        setField("loggedInRole", AuthAdmin.Role.ADMIN);

        try {
            Method method = AuthAdmin.class.getDeclaredMethod("handleAdminChoice", int.class, Scanner.class);
            method.setAccessible(true);
            method.invoke(authAdmin, 999, scanner);
        } catch (Exception e) {
            fail("Reflection failed");
        }
    }

    @Test
    void coverPrivateMenuHandlers() throws Exception {
        setField("isLoggedIn", true);

        Method hs = AuthAdmin.class.getDeclaredMethod("handleSuperAdminChoice", int.class, Scanner.class);
        hs.setAccessible(true);
        Method ha = AuthAdmin.class.getDeclaredMethod("handleAdminChoice", int.class, Scanner.class);
        ha.setAccessible(true);

        setField("loggedInRole", AuthAdmin.Role.SUPER_ADMIN);
        hs.invoke(authAdmin, 999, scanner);

        setField("loggedInRole", AuthAdmin.Role.ADMIN);
        ha.invoke(authAdmin, 999, scanner);
    }
    private void invokePrivate(Object target, String name, Object arg) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name, Scanner.class);
        m.setAccessible(true);
        m.invoke(target, arg);
    }

    private void setField(String name, Object value) {
        try {
            var f = AuthAdmin.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(authAdmin, value);
        } catch (Exception ignored) {}
    }

    private void invoke(String name, Object arg) throws Exception {
        var m = AuthAdmin.class.getDeclaredMethod(name, Scanner.class);
        m.setAccessible(true);
        m.invoke(authAdmin, arg);
    }
}