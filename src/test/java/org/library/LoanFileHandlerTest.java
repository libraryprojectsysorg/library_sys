package org.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.Book;
import org.library.Domain.Loan;
import org.library.Domain.User;

import org.library.Service.Strategy.BookFileHandler;
import org.library.Service.Strategy.LoanFileHandler;
import org.library.Service.Strategy.UserFileHandler;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
}
