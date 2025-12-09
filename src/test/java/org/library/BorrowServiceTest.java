package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.domain.*;
import org.library.Service.strategy.BorrowService;
import org.library.Service.strategy.EmailNotifier;
import org.library.Service.strategy.LoanFileHandler;
import org.library.Service.strategy.fines.FineStrategy;
import org.library.exception.MediaAlreadyBorrowedException;
import org.library.exception.MediaNotAvailableException;
import org.library.exception.UserCannotBorrowException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BorrowServiceTest {

    @Mock
    private EmailNotifier emailNotifier;
    @Mock
    private LoanFileHandler loanFileHandler;
    @Mock
    private User user;
    @Mock
    private Book book;
    @Mock
    private FineStrategy fineStrategy;

    private BorrowService borrowService;

    private final String FIXED_DATE = "2023-01-01T10:00:00Z";
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse(FIXED_DATE), ZoneId.systemDefault());
        borrowService = new BorrowService(emailNotifier, loanFileHandler);
        borrowService.setClock(fixedClock);
    }

    @Test
    void shouldBorrowBookSuccessfully_WhenAllConditionsMet() throws MediaNotAvailableException, UserCannotBorrowException, MediaAlreadyBorrowedException {
        when(book.isAvailable()).thenReturn(true);
        when(book.getIsbn()).thenReturn("12345");
        when(book.getLoanDays()).thenReturn(14);
        when(user.hasUnpaidFines()).thenReturn(false);
        when(loanFileHandler.isMediaBorrowed("12345")).thenReturn(false);

        Loan loan = borrowService.borrowMedia(book, user);

        assertNotNull(loan);
        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getMedia());
        assertEquals(LocalDate.now(fixedClock), loan.getBorrowDate());

        verify(loanFileHandler).saveLoan(any(Loan.class));
        verify(book).setAvailable(false);
    }

    @Test
    void shouldFailToBorrow_WhenBookIsNotAvailable() {
        when(book.isAvailable()).thenReturn(false);

        MediaNotAvailableException exception = assertThrows(MediaNotAvailableException.class, () -> {
            borrowService.borrowMedia(book, user);
        });

        assertEquals("Book/CD not available", exception.getMessage());
        verify(loanFileHandler, never()).saveLoan(any());
    }

    @Test
    void shouldFailToBorrow_WhenUserHasUnpaidFines() {
        when(book.isAvailable()).thenReturn(true);
        when(user.hasUnpaidFines()).thenReturn(true);

        UserCannotBorrowException exception = assertThrows(UserCannotBorrowException.class, () -> {
            borrowService.borrowMedia(book, user);
        });

        assertTrue(exception.getMessage().contains("Cannot borrow"));
        verify(loanFileHandler, never()).saveLoan(any());
    }

    @Test
    void shouldFailToBorrow_WhenMediaAlreadyBorrowed() {
        when(book.isAvailable()).thenReturn(true);
        when(book.getIsbn()).thenReturn("12345");
        when(user.hasUnpaidFines()).thenReturn(false);
        when(loanFileHandler.loadAllLoans()).thenReturn(List.of());
        when(loanFileHandler.isMediaBorrowed("12345")).thenReturn(true);

        MediaAlreadyBorrowedException ex = assertThrows(MediaAlreadyBorrowedException.class, () -> {
            borrowService.borrowMedia(book, user);
        });

        assertTrue(ex.getMessage().contains("already borrowed"));
    }

    @Test
    void shouldCalculateFine_WhenReturningOverdueBook() {
        LocalDate pastDueDate = LocalDate.now(fixedClock).minusDays(5);
        String loanId = "LOAN_123";

        Loan overdueLoan = mock(Loan.class);
        when(overdueLoan.getLoanId()).thenReturn(loanId);
        when(overdueLoan.getDueDate()).thenReturn(pastDueDate);
        when(overdueLoan.getMedia()).thenReturn(book);
        when(overdueLoan.getUser()).thenReturn(user);

        when(book.getFineStrategy()).thenReturn(fineStrategy);
        when(fineStrategy.calculateFine(5)).thenReturn(50);

        List<Loan> activeLoans = new ArrayList<>();
        activeLoans.add(overdueLoan);
        when(loanFileHandler.loadAllLoans()).thenReturn(activeLoans);

        int fine = borrowService.returnMedia(loanId);

        assertEquals(50, fine);
        verify(book).setAvailable(true);
        verify(loanFileHandler).rewriteAllLoans(anyList());
    }

    @Test
    void hasActiveLoans_ShouldReturnTrue_WhenUserHasLoan() {
        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(user);
        when(loanFileHandler.loadAllLoans()).thenReturn(List.of(loan));

        assertTrue(borrowService.hasActiveLoans(user));
    }

    @Test
    void hasActiveLoans_ShouldReturnFalse_WhenNoLoansForUser() {
        when(loanFileHandler.loadAllLoans()).thenReturn(List.of());

        assertFalse(borrowService.hasActiveLoans(user));
    }

    @Test
    void addLoan_ShouldSaveLoanViaFileHandler() {
        Loan loan = mock(Loan.class);
        borrowService.addLoan(loan);
        verify(loanFileHandler).saveLoan(loan);
    }

    @Test
    void unregisterUser_ShouldRemoveAllLoansOfUser() {
        User userToRemove = mock(User.class);
        when(userToRemove.getId()).thenReturn("U999");

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn("U888");

        Loan loan1 = mock(Loan.class);
        Loan loan2 = mock(Loan.class);
        when(loan1.getUser()).thenReturn(userToRemove);
        when(loan2.getUser()).thenReturn(otherUser);

        when(loanFileHandler.loadAllLoans()).thenReturn(new ArrayList<>(List.of(loan1, loan2)));

        boolean result = borrowService.unregisterUser("U999");

        assertTrue(result);
        verify(loanFileHandler).rewriteAllLoans(argThat(list -> list.size() == 1));
    }

    @Test
    void unregisterUser_ShouldReturnFalse_WhenUserHasNoLoans() {
        when(loanFileHandler.loadAllLoans()).thenReturn(new ArrayList<>());
        assertFalse(borrowService.unregisterUser("U000"));
        verify(loanFileHandler, never()).rewriteAllLoans(any());
    }

    @Test
    void returnLoan_ShouldReturnTrue_OnSuccessfulReturn() {
        Loan loan = mock(Loan.class);
        when(loan.getLoanId()).thenReturn("LOAN123");
        when(loan.getMedia()).thenReturn(book);
        when(loan.getUser()).thenReturn(user);
        when(loan.getDueDate()).thenReturn(LocalDate.now(fixedClock).plusDays(10));
        when(loanFileHandler.loadAllLoans()).thenReturn(new ArrayList<>(List.of(loan)));

        boolean result = borrowService.returnLoan("LOAN123");

        assertTrue(result);
        verify(book).setAvailable(true);
        verify(loanFileHandler).rewriteAllLoans(anyList());
    }

    @Test
    void returnLoan_ShouldReturnFalse_WhenLoanNotFound() {
        when(loanFileHandler.loadAllLoans()).thenReturn(List.of());
        assertFalse(borrowService.returnLoan("NONEXISTENT"));
    }

    @Test
    void getUsersWithOverdueLoans_ShouldReturnDistinctUsers() {
        LocalDate past = LocalDate.now(fixedClock).minusDays(10);

        Loan loan1 = mock(Loan.class);
        Loan loan2 = mock(Loan.class);
        Loan loan3 = mock(Loan.class);

        when(loan1.getUser()).thenReturn(user);
        when(loan2.getUser()).thenReturn(user);
        when(loan3.getUser()).thenReturn(mock(User.class));

        when(loan1.getDueDate()).thenReturn(past);
        when(loan2.getDueDate()).thenReturn(past);
        when(loan3.getDueDate()).thenReturn(past);

        when(loanFileHandler.loadAllLoans()).thenReturn(List.of(loan1, loan2, loan3));

        assertEquals(2, borrowService.getUsersWithOverdueLoans().size());
    }

    @Test
    void countOverdueLoansForUser_ShouldReturnCorrectCount() {
        LocalDate past = LocalDate.now(fixedClock).minusDays(10);
        LocalDate future = LocalDate.now(fixedClock).plusDays(5);

        Loan overdue1 = mock(Loan.class);
        Loan overdue2 = mock(Loan.class);
        Loan notOverdue = mock(Loan.class);

        when(overdue1.getUser()).thenReturn(user);
        when(overdue2.getUser()).thenReturn(user);
        when(notOverdue.getUser()).thenReturn(user);

        when(overdue1.getDueDate()).thenReturn(past);
        when(overdue2.getDueDate()).thenReturn(past);
        when(notOverdue.getDueDate()).thenReturn(future);

        when(loanFileHandler.loadAllLoans()).thenReturn(List.of(overdue1, overdue2, notOverdue));

        assertEquals(2, borrowService.countOverdueLoansForUser(user));
    }

    @Test
    void borrowCD_ShouldThrowException_WhenCDOrUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> borrowService.borrowCD(null, user));
        assertThrows(IllegalArgumentException.class, () -> borrowService.borrowCD(mock(CD.class), null));
    }

    @Test
    void borrowCD_ShouldDelegateToBorrowMedia_WhenValid() throws MediaNotAvailableException, UserCannotBorrowException, MediaAlreadyBorrowedException {
        CD cd = mock(CD.class);
        when(cd.isAvailable()).thenReturn(true);
        when(cd.getIsbn()).thenReturn("CD123");
        when(cd.getLoanDays()).thenReturn(7);
        when(user.hasUnpaidFines()).thenReturn(false);
        when(loanFileHandler.isMediaBorrowed("CD123")).thenReturn(false);

        Loan loan = borrowService.borrowCD(cd, user);

        assertNotNull(loan);
        verify(loanFileHandler).saveLoan(any(Loan.class));
        verify(cd).setAvailable(false);
    }
}
