package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.Book;
import org.library.Domain.Loan;
import org.library.Domain.User;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.EmailNotifier;
import org.library.Service.Strategy.LoanFileHandler;
import org.library.Service.Strategy.fines.FineStrategy;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldBorrowBookSuccessfully_WhenAllConditionsMet() {

        when(book.isAvailable()).thenReturn(true);
        when(book.getIsbn()).thenReturn("12345");
        when(book.getLoanDays()).thenReturn(14);
        when(user.hasUnpaidFines()).thenReturn(false);

        when(loanFileHandler.isMediaBorrowed("12345")).thenReturn(false);

        Loan loan = borrowService.borrowMedia(book, user);


        assertNotNull(loan, "يجب إنشاء كائن إعارة");
        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getMedia());
        assertEquals(LocalDate.now(fixedClock), loan.getBorrowDate(), "تاريخ الإعارة يجب أن يكون اليوم المحدد بالساعة الثابتة");


        verify(loanFileHandler).saveLoan(any(Loan.class));
        verify(book).setAvailable(false);
    }

    @Test
    void shouldFailToBorrow_WhenBookIsNotAvailable() {

        when(book.isAvailable()).thenReturn(false);


        Exception exception = assertThrows(RuntimeException.class, () -> {
            borrowService.borrowMedia(book, user);
        });

        assertEquals("Book not available", exception.getMessage());
        verify(loanFileHandler, never()).saveLoan(any());
    }

    @Test
    void shouldFailToBorrow_WhenUserHasUnpaidFines() {

        when(book.isAvailable()).thenReturn(true);
        when(user.hasUnpaidFines()).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            borrowService.borrowMedia(book, user);
        });

        assertTrue(exception.getMessage().contains("Cannot borrow"));
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


        assertEquals(50, fine, "يجب حساب الغرامة بـ 50");
        verify(book).setAvailable(true);
        verify(loanFileHandler).rewriteAllLoans(anyList());
    }
}