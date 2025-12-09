package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Service.strategy.*;
import org.library.domain.*;
import org.library.Service.strategy.fines.FineCalculator;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAdminTest {

    @Mock private BorrowService borrowService;
    @Mock private ReminderService reminderService ;
    @Mock private FineCalculator fineCalculator;
    @Mock private BookCDService bookCDService;

    private AuthAdmin authAdmin;

    @BeforeEach
    void setUp() throws Exception {
        authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookCDService);

        // نفرغ الـ users list ونحقن قايمة جديدة عشان نتحكم فيها
        Field usersField = AuthAdmin.class.getDeclaredField("users");
        usersField.setAccessible(true);
        usersField.set(authAdmin, new ArrayList<User>());
    }

    @Test
    void superAdminShouldLoginSuccessfully() {
        // نعمل mock للـ static method بطريقة بسيطة جدًا
        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            User superAdmin = new User("SA001", "Library Super Admin", "default_super@library.com");
            setPrivateField(superAdmin, "role", "SUPER_ADMIN");

            mocked.when(() -> UserFileHandler.getUserByCredentials("default_super@library.com", "default_superpass123"))
                    .thenReturn(superAdmin);

            boolean result = authAdmin.login("default_super@library.com", "default_superpass123");

            assertTrue(result);
            assertTrue(authAdmin.isSuperAdmin());
        }
    }

    @Test
    void invalidLoginShouldFail() {
        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            mocked.when(() -> UserFileHandler.getUserByCredentials(anyString(), anyString()))
                    .thenReturn(null);

            assertFalse(authAdmin.login("wrong@wrong.com", "wrong"));
            assertFalse(authAdmin.isLoggedInAdmin());
        }
    }

    @Test
    void addAdminShouldReturnTrue() {
        loginAsSuperAdmin();

        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            mocked.when(() -> UserFileHandler.saveUser("a@admin.com", "123", "ADMIN", "A001", "Admin"))
                    .thenReturn(true);

            boolean result = authAdmin.addAdmin("a@admin.com", "123", "A001", "Admin");

            assertTrue(result);
        }
    }

    @Test
    void deleteAdminShouldWork() {
        loginAsSuperAdmin();

        User adminUser = mock(User.class);
        when(adminUser.getId()).thenReturn("A999");
        when(adminUser.getRole()).thenReturn("ADMIN");
        addUserToList(adminUser);

        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            mocked.when(() -> UserFileHandler.removeUserById("A999", "SUPER_ADMIN")).thenReturn(true);

            assertTrue(authAdmin.deleteAdmin("A999"));
        }
    }

    @Test
    void unregisterUserShouldSucceedWhenClean() {
        loginAsSuperAdmin();

        User normalUser = mock(User.class);
        when(normalUser.getId()).thenReturn("U888");
        when(normalUser.getRole()).thenReturn("USER");
        when(normalUser.hasUnpaidFines()).thenReturn(false);
        addUserToList(normalUser);

        when(borrowService.hasActiveLoans(normalUser)).thenReturn(false);

        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            mocked.when(() -> UserFileHandler.removeUserById("U888", "SUPER_ADMIN")).thenReturn(true);

            assertTrue(authAdmin.unregisterUser("U888"));
        }
    }

    @Test
    void payAllUserFines_ShouldReturnTrue_WhenHasUnpaidFines() throws Exception {

        User user = new User("U001", "Test User", "test@example.com");


        Fine unpaidFine = new Fine(100);


        Field finesField = User.class.getDeclaredField("fines");
        finesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Fine> fines = (List<Fine>) finesField.get(user);
        fines.add(unpaidFine);


        User spyUser = spy(user);

        boolean result = authAdmin.payAllUserFines(spyUser);

        assertTrue(result);
        verify(spyUser).payFine(unpaidFine);
    }
    @Test
    void getAllBooksShouldReturnList() {
        when(bookCDService.searchBooks("")).thenReturn(List.of(mock(Book.class)));
        assertEquals(1, authAdmin.getAllBooks().size());
    }

    @Test
    void borrowMediaShouldSucceed() {
        loginAsSuperAdmin();
        Media media = mock(Media.class);
        User user = mock(User.class);

        when(borrowService.borrowMedia(media, user)).thenReturn(mock(Loan.class));

        assertTrue(authAdmin.borrowMedia(media, user));
    }

    @Test
    void getUserTotalFine_ShouldReturnMinusOne_WhenUserNotFound() {
        assertEquals(-1, authAdmin.getUserTotalFine("ANY_ID"));
    }

    @Test
    void getUserTotalFine_ShouldReturnFineAmount_WhenUserExists() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn("TEST123");

        // نحقن اليوزر في الـ list
        Field field = AuthAdmin.class.getDeclaredField("users");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> list = (List<User>) field.get(authAdmin);
        list.add(user);

        when(fineCalculator.calculateTotalFine(user)).thenReturn(300);

        assertEquals(300, authAdmin.getUserTotalFine("TEST123"));
    }


    @Test
    void getUserTotalFine_ShouldReturnCalculatedFine_WhenUserExists() throws Exception {

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("U999");

        Field usersField = AuthAdmin.class.getDeclaredField("users");
        usersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> usersList = (List<User>) usersField.get(authAdmin);
        usersList.add(mockUser);


        when(fineCalculator.calculateTotalFine(mockUser)).thenReturn(750);

        int result = authAdmin.getUserTotalFine("U999");

        assertEquals(750, result);
    }


    @Test
    void getUserTotalFine_ShouldReturnCalculatedAmount() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn("U999");


        Field f = AuthAdmin.class.getDeclaredField("users");
        f.setAccessible(true);
        ((List<User>) f.get(authAdmin)).add(user);

        when(fineCalculator.calculateTotalFine(user)).thenReturn(999);

        assertEquals(999, authAdmin.getUserTotalFine("U999"));
    }

    @Test
    void borrowMedia_ShouldReturnTrue_OnSuccess() {
        Media media = mock(Media.class);
        User user = mock(User.class);
        Loan loan = mock(Loan.class);

        when(borrowService.borrowMedia(media, user)).thenReturn(loan);

        assertTrue(authAdmin.borrowMedia(media, user));
    }

    @Test
    void borrowMedia_ShouldReturnFalse_OnException() {
        Media media = mock(Media.class);
        User user = mock(User.class);

        when(borrowService.borrowMedia(media, user))
                .thenThrow(new RuntimeException("Not available"));

        assertFalse(authAdmin.borrowMedia(media, user));
    }
    @Test
    void addBook_ShouldDelegateToBookCDService() {
        when(bookCDService.addBook("Java", "Ahmad", "111")).thenReturn(true);

        assertTrue(authAdmin.addBook("Java", "Ahmad", "111"));

        verify(bookCDService).addBook("Java", "Ahmad", "111");
    }

    @Test
    void deleteBook_ShouldDelegateToBookCDService() {
        when(bookCDService.removeByIsbn("111")).thenReturn(true);

        assertTrue(authAdmin.deleteBook("111"));

        verify(bookCDService).removeByIsbn("111");
    }

    @Test
    void getAllBooks_ShouldDelegateToBookCDService() {
        Book mockBook = mock(Book.class);
        when(bookCDService.searchBooks("")).thenReturn(List.of(mockBook));

        List<Book> result = authAdmin.getAllBooks();

        assertEquals(1, result.size());
        verify(bookCDService).searchBooks("");
    }

    @Test
    void addCD_ShouldDelegateToBookCDService() {
        when(bookCDService.addCD("Album", "Artist", "CD111")).thenReturn(true);

        assertTrue(authAdmin.addCD("Album", "Artist", "CD111"));

        verify(bookCDService).addCD("Album", "Artist", "CD111");
    }

    @Test
    void deleteCD_ShouldDelegateToBookCDService() {
        when(bookCDService.removeCDByCode("CD111")).thenReturn(true);

        assertTrue(authAdmin.deleteCD("CD111"));

        verify(bookCDService).removeCDByCode("CD111");
    }

    @Test
    void getAllCDs_ShouldDelegateToBookCDService() {
        CD mockCD = mock(CD.class);
        when(bookCDService.searchCD("")).thenReturn(List.of(mockCD));

        List<CD> result = authAdmin.getAllCDs();

        assertEquals(1, result.size());
        verify(bookCDService).searchCD("");
    }
    @Test
    void getUserTotalFine_ShouldHandleFineCalculatorException() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn("U123");


        Field f = AuthAdmin.class.getDeclaredField("users");
        f.setAccessible(true);
        ((List<User>) f.get(authAdmin)).add(user);


        when(fineCalculator.calculateTotalFine(user))
                .thenThrow(new RuntimeException("DB down"));


        assertThrows(RuntimeException.class, () ->
                authAdmin.getUserTotalFine("U123")
        );
    }

    @Test
    void payAllUserFines_ShouldHandleEmptyFinesList() {
        User user = mock(User.class);
        when(user.getFines()).thenReturn(new ArrayList<>());

        boolean result = authAdmin.payAllUserFines(user);

        assertFalse(result);
        verify(user, never()).payFine(any());
    }


    @Test
    void borrowMedia_ShouldReturnTrue_WhenBorrowServiceReturnsLoan() {
        Media media = mock(Media.class);
        User user = mock(User.class);
        when(borrowService.borrowMedia(media, user)).thenReturn(mock(Loan.class));

        assertTrue(authAdmin.borrowMedia(media, user));
    }

    @Test
    void returnMedia_ShouldReturnZero_WhenNoFine() {
        Loan loan = mock(Loan.class);
        when(loan.getLoanId()).thenReturn("L000");
        when(borrowService.returnMedia("L000")).thenReturn(0);

        assertEquals(0, authAdmin.returnMedia(loan));
    }

    @Test
    void login_ShouldHandleAllRoleCases_IncludingDefault() throws Exception {
        // 1. ADMIN
        try (MockedStatic<UserFileHandler> m = Mockito.mockStatic(UserFileHandler.class)) {
            User u = mock(User.class);
            when(u.getRole()).thenReturn("admin");
            m.when(() -> UserFileHandler.getUserByCredentials(any(), any())).thenReturn(u);
            authAdmin.login("a", "a");
            assertTrue(authAdmin.isLoggedInAdmin());
        }

        // 2. USER
        try (MockedStatic<UserFileHandler> m = Mockito.mockStatic(UserFileHandler.class)) {
            User u = mock(User.class);
            when(u.getRole()).thenReturn("User");
            m.when(() -> UserFileHandler.getUserByCredentials(any(), any())).thenReturn(u);
            authAdmin.login("a", "a");
            assertTrue(authAdmin.isLoggedInUser());
        }

        // 3. Unknown role → default case
        try (MockedStatic<UserFileHandler> m = Mockito.mockStatic(UserFileHandler.class)) {
            User u = mock(User.class);
            when(u.getRole()).thenReturn("HACKER");
            m.when(() -> UserFileHandler.getUserByCredentials(any(), any())).thenReturn(u);
            authAdmin.login("a", "a");

            Field f = AuthAdmin.class.getDeclaredField("loggedInRole");
            f.setAccessible(true);
            assertNull(f.get(authAdmin));
        }
    }

    @Test
    void loadUsers_ShouldClearAndReloadFromFileHandler() throws Exception {
        // نضيف يوزر وهمي في الـ list
        Field usersField = AuthAdmin.class.getDeclaredField("users");
        usersField.setAccessible(true);
        List<User> usersList = (List<User>) usersField.get(authAdmin);
        usersList.add(mock(User.class)); // عشان نتحقق إن clear اشتغلت

        // نموك الـ loadAllUsers
        List<User> newUsers = List.of(mock(User.class), mock(User.class));
        try (MockedStatic<UserFileHandler> m = mockStatic(UserFileHandler.class)) {
            m.when(UserFileHandler::loadAllUsers).thenReturn(newUsers);

            authAdmin.loadUsers();

            assertEquals(2, usersList.size());
        }
    }

    @Test
    void isLoggedInUser_ShouldReturnTrueOnlyForUserRole() throws Exception {
        Field loggedIn = AuthAdmin.class.getDeclaredField("isLoggedIn");
        Field role = AuthAdmin.class.getDeclaredField("loggedInRole");
        loggedIn.setAccessible(true);
        role.setAccessible(true);

        loggedIn.set(authAdmin, true);
        role.set(authAdmin, AuthAdmin.Role.USER);
        assertTrue(authAdmin.isLoggedInUser());

        role.set(authAdmin, AuthAdmin.Role.ADMIN);
        assertFalse(authAdmin.isLoggedInUser());

        loggedIn.set(authAdmin, false);
        assertFalse(authAdmin.isLoggedInUser());
    }

    @Test
    void logout_ShouldResetAllFields() throws Exception {
        // نعمل login
        try (MockedStatic<UserFileHandler> m = mockStatic(UserFileHandler.class)) {
            User u = mock(User.class);
            when(u.getRole()).thenReturn("ADMIN");
            m.when(() -> UserFileHandler.getUserByCredentials(any(), any())).thenReturn(u);
            authAdmin.login("a", "a");
        }

        authAdmin.logout();

        Field f1 = AuthAdmin.class.getDeclaredField("isLoggedIn");
        Field f2 = AuthAdmin.class.getDeclaredField("loggedInEmail");
        Field f3 = AuthAdmin.class.getDeclaredField("loggedInRole");
        f1.setAccessible(true); f2.setAccessible(true); f3.setAccessible(true);

        assertFalse((Boolean) f1.get(authAdmin));
        assertNull(f2.get(authAdmin));
        assertNull(f3.get(authAdmin));
    }
    private void loginAsSuperAdmin() {
        try (MockedStatic<UserFileHandler> mocked = Mockito.mockStatic(UserFileHandler.class)) {
            User su = new User("SA001", "Super", "default_super@library.com");
            setPrivateField(su, "role", "SUPER_ADMIN");
            mocked.when(() -> UserFileHandler.getUserByCredentials(anyString(), anyString())).thenReturn(su);
            authAdmin.login("x", "x");
        }
    }

    private void addUserToList(User user) {
        try {
            Field f = AuthAdmin.class.getDeclaredField("users");
            f.setAccessible(true);
            List<User> list = (List<User>) f.get(authAdmin);
            list.add(user);
        } catch (Exception ignored) {}
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignored) {}
    }
}