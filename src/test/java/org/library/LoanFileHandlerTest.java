package org.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.domain.Book;
import org.library.domain.Loan;
import org.library.domain.User;

import org.library.Service.strategy.BookFileHandler;
import org.library.Service.strategy.LoanFileHandler;
import org.library.Service.strategy.UserFileHandler;


import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanFileHandlerTest {

    private static final String TEST_LOANS_FILE = "test_loans.txt";
    private static final String TEST_USERS_FILE = "test_users_for_loans.txt";
    private static final String TEST_BOOKS_FILE = "test_books_for_loans.txt";

    private LoanFileHandler loanFileHandler;

    @BeforeEach
    void setUp() {

        LoanFileHandler.setLoansFile(TEST_LOANS_FILE);
        UserFileHandler.setUsersFile(TEST_USERS_FILE);
        BookFileHandler.setBooksFile(TEST_BOOKS_FILE);

        cleanUpFiles();


        UserFileHandler.saveUser("test@user.com", "pass", "USER", "U01", "Test User");


        BookFileHandler.saveBook(new Book("Test Book", "Author", "12345"));

        loanFileHandler = new LoanFileHandler();
    }

    @AfterEach
    void tearDown() {
        cleanUpFiles();
    }

    private void cleanUpFiles() {
        new File(TEST_LOANS_FILE).delete();
        new File(TEST_USERS_FILE).delete();
        new File(TEST_BOOKS_FILE).delete();
    }

    @Test
    void shouldSaveAndLoadLoanCorrectly() {

        User user = UserFileHandler.getUserByCredentials("test@user.com", "pass");
        Book book = BookFileHandler.loadAllBooks().get(0);

        Loan loan = new Loan("L01", book, user, LocalDate.now(), LocalDate.now().plusDays(14));

        loanFileHandler.saveLoan(loan);
        List<Loan> loadedLoans = loanFileHandler.loadAllLoans();

        assertNotNull(loadedLoans);
        assertEquals(1, loadedLoans.size(), "يجب أن يتم تحميل إعارة واحدة");

        Loan loadedLoan = loadedLoans.get(0);
        assertEquals("L01", loadedLoan.getLoanId());
        assertEquals("U01", loadedLoan.getUser().getId());
        assertEquals("12345", loadedLoan.getMedia().getIsbn());
        assertEquals(LocalDate.now(), loadedLoan.getBorrowDate(), "تاريخ الإعارة يجب أن يطابق اليوم");
    }

    @Test
    void shouldIdentifyBorrowedMediaCorrectly() {

        User user = UserFileHandler.getUserByCredentials("test@user.com", "pass");
        Book book = BookFileHandler.loadAllBooks().get(0);
        Loan loan = new Loan("L02", book, user, LocalDate.now(), LocalDate.now().plusDays(14));


        loanFileHandler.saveLoan(loan);


        boolean isBorrowed = loanFileHandler.isMediaBorrowed("12345");
        boolean isNotBorrowed = loanFileHandler.isMediaBorrowed("99999");


        assertTrue(isBorrowed, "الكتاب يجب أن يكون مسجلاً كمستعار");
        assertFalse(isNotBorrowed, "الكتاب غير الموجود يجب ألا يكون مستعاراً");
    }

    @Test
    void shouldRewriteLoansCorrectly() {

        User user = UserFileHandler.getUserByCredentials("test@user.com", "pass");
        Book book = BookFileHandler.loadAllBooks().get(0);

        Loan loan1 = new Loan("L1", book, user, LocalDate.now(), LocalDate.now().plusDays(10));
        Loan loan2 = new Loan("L2", book, user, LocalDate.now(), LocalDate.now().plusDays(10));


        loanFileHandler.rewriteAllLoans(List.of(loan1, loan2));
        List<Loan> result = loanFileHandler.loadAllLoans();


        assertEquals(2, result.size());
        assertEquals("L1", result.get(0).getLoanId());
        assertEquals("L2", result.get(1).getLoanId());
    }
    @Test
    void loadAllLoans_ShouldReturnEmptyList_WhenFileDoesNotExist() {

        LoanFileHandler.setLoansFile("this_file_definitely_does_not_exist_12345.txt");

        List<Loan> loans = loanFileHandler.loadAllLoans();

        assertTrue(loans.isEmpty());

    }

    @Test
    void loadAllLoans_ShouldSkipInvalidLines_AndContinue() throws Exception {
        // نكتب سطر تالف في الملف يدويًا
        try (PrintWriter writer = new PrintWriter(TEST_LOANS_FILE)) {
            writer.println("L01,12345,U01,2025-01-01,invalid-date");
            writer.println("L02,12345,U01,2025-01-01,2025-01-15");
        }

        List<Loan> loans = loanFileHandler.loadAllLoans();


        assertEquals(1, loans.size());

    }

    @Test
    void loadAllLoans_ShouldHandleIOException_Gracefully() {

        LoanFileHandler.setLoansFile("/invalid/path/loans.txt");

        List<Loan> loans = loanFileHandler.loadAllLoans();

        assertTrue(loans.isEmpty());

    }

    @Test
    void loadAllLoans_ShouldSkipLoan_WhenUserOrMediaNotFound() throws Exception {

        try (PrintWriter writer = new PrintWriter(TEST_LOANS_FILE)) {
            writer.println("L99,99999,U99,2025-01-01,2025-01-15");
        }

        List<Loan> loans = loanFileHandler.loadAllLoans();
        assertTrue(loans.isEmpty(), "الإعارة اللي اليوزر أو الكتاب مش موجودين لازم تتجاهل");


    }

    @Test
    void isMediaBorrowed_ShouldReturnFalse_WhenLoanIsReturned() throws Exception {

        User user = UserFileHandler.getUserByCredentials("test@user.com", "pass");
        Book book = BookFileHandler.loadAllBooks().get(0);

        Loan returnedLoan = new Loan("L88", book, user, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));


        java.lang.reflect.Field f = Loan.class.getDeclaredField("returned");
        f.setAccessible(true);
        f.set(returnedLoan, true);

        LoanFileHandler handler = spy(new LoanFileHandler());
        doReturn(List.of(returnedLoan)).when(handler).loadAllLoans();

        boolean result = handler.isMediaBorrowed("12345");

        assertFalse(result, "الإعارة المرتجعة ما تُعتبرش مستعارة");
    }
}