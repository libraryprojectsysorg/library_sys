package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.*;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.EmailNotifier;
import org.library.Service.Strategy.LoanFileHandler;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowTest {

    private BorrowService borrowService;
    private User user;
    private Book book;
    private CD cd;
    private LoanFileHandler mockFileHandler;

    @BeforeEach
    void setup() {
        mockFileHandler = mock(LoanFileHandler.class);
        borrowService = new BorrowService(null, mockFileHandler);

        user = new User("U001", "Weam Ahmad", "weam@example.com");
        book = new Book("Java Programming", "John Doe", "ISBN1234");
        cd = new CD("Top Hits", "DJ Mix", "CD5678");
    }

    @Test
    void testBorrowBookNoFines() {
        Loan loan = borrowService.borrowMedia(book, user);

        verify(mockFileHandler).saveLoan(loan);
        assertNotNull(loan);
        assertFalse(book.isAvailable());
        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getMedia());
    }

    @Test
    void testBorrowBookWithUnpaidFines() {
        Fine fine = new Fine(10);
        user.addFine(fine);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                borrowService.borrowMedia(book, user)
        );

        assertEquals("Cannot borrow: overdue books or unpaid fines", exception.getMessage());
    }

    @Test
    void testReturnBookNoFine() {
        Loan loan = borrowService.borrowMedia(book, user);

        // تم التعديل: استخدم ArrayList بدل List.of()
        when(mockFileHandler.loadAllLoans()).thenReturn(new ArrayList<>(List.of(loan)));

        int fine = borrowService.returnMedia(loan.getLoanId());

        assertEquals(0, fine);
        assertTrue(book.isAvailable());
        assertFalse(user.hasUnpaidFines());
        verify(mockFileHandler).rewriteAllLoans(Collections.emptyList());
    }

    @Test
    void testReturnBookWithFine() {
        LocalDate pastDate = LocalDate.now().minusDays(book.getLoanDays() + 5);
        Clock mockClock = Clock.fixed(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        borrowService.setClock(mockClock);

        Loan loan = borrowService.borrowMedia(book, user);

        // تم التعديل: استخدم ArrayList
        when(mockFileHandler.loadAllLoans()).thenReturn(new ArrayList<>(List.of(loan)));

        borrowService.setClock(Clock.systemDefaultZone());

        int fine = borrowService.returnMedia(loan.getLoanId());

        assertTrue(fine > 0);
        assertTrue(user.hasUnpaidFines());
        assertTrue(book.isAvailable());
    }

    @Test
    void testBorrowCD() {
        Loan loan = borrowService.borrowMedia(cd, user);

        verify(mockFileHandler).saveLoan(loan);
        assertNotNull(loan);
        assertFalse(cd.isAvailable());
        assertEquals(cd, loan.getMedia());
    }
}