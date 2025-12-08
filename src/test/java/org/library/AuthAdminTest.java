package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.*;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
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