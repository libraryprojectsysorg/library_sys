package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.*;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.EmailNotifier;
import org.library.Service.Strategy.fines.BookFineStrategy;
import org.library.Service.Strategy.fines.FineStrategy;
import org.library.Service.Strategy.fines.CDFineStrategy;


import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Tests for BorrowService (US2.1, US2.2, US4.1, US5.1).
 * Uses Mockito for time mocking in overdue detection.
 * Covers borrowing success, unavailable books, restrictions, overdue logic, polymorphism.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class BorrowTest {
    private BorrowService service;
    private Clock originalClock;  // لاستعادة الـ Clock الأصلي

    private EmailNotifier emailNotifier;
    @BeforeEach
    public void setUp() {


        service = new BorrowService(emailNotifier);
        originalClock = service.getClock();  // حفظ الأصلي إذا كان setter/getter موجود
    }

    @Test
    public void testBorrowBookSuccess() {  // Test US2.1 success
        // Arrange
        User user = new User("U001", "John Doe", "john@example.com");
        Book book = new Book("Title", "Author", "123");
        book.setAvailable(true);

        // Act
        Loan loan = service.borrowMedia(book, user);  // Use borrowMedia (general)

        // Assert
        assertNotNull(loan);
        assertEquals(user, loan.getUser());
        assertFalse(book.isAvailable());
        assertEquals(LocalDate.now().plusDays(28), loan.getDueDate());  // +28 days
    }

    @Test
    public void testBorrowUnavailableBook() {  // Edge case
        User user = new User("U002", "Jane Doe", "jane@example.com");
        Book book = new Book("Title2", "Author2", "456");
        book.setAvailable(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.borrowMedia(book, user);
        });
        assertEquals("Book not available", exception.getMessage());
    }

    @Test
    public void testOverdueDetectionTrue() {  // US2.2 true
        User user = new User("U003", "Overdue User", "overdue@example.com");
        Book book = new Book("Overdue Book", "Author", "789");
        LocalDate borrowDate = LocalDate.now().minusDays(30);
        Loan loan = new Loan("L001", book, user, borrowDate, borrowDate.plusDays(28));

        assertTrue(service.isOverdue(loan));
    }

    @Test
    public void testOverdueDetectionFalse() {  // US2.2 false
        User user = new User("U004", "OnTime User", "ontime@example.com");
        Book book = new Book("OnTime Book", "Author", "101");
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        Loan loan = new Loan("L002", book, user, borrowDate, borrowDate.plusDays(28));

        assertFalse(service.isOverdue(loan));
    }

    @Test
    public void testOverdueWithMockTime() {  // Time manipulation (US2.2)
        LocalDate borrowDate = LocalDate.of(2025, 10, 7);
        LocalDate dueDate = borrowDate.plusDays(28);
        Instant advancedInstant = dueDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Clock mockClock = Clock.fixed(advancedInstant, ZoneOffset.UTC);
        service.setClock(mockClock);

        User user = new User("U005", "Mock User", "mock@example.com");
        Book book = new Book("Mock Book", "Author", "202");
        Loan loan = new Loan("L003", book, user, borrowDate, dueDate);

        assertTrue(service.isOverdue(loan));  // Mock now > due
    }

    @Test
    public void testOverdueWithMockedClock() {
        Clock mockClock = Clock.fixed(Instant.parse("2025-11-05T00:00:00Z"), ZoneOffset.UTC);
        service.setClock(mockClock);
        LocalDate fixedDate = LocalDate.now(mockClock);
        User user = new User("U006", "Clock User", "clock@example.com");
        Book book = new Book("Clock Book", "Author", "303");
        LocalDate borrowDate = fixedDate.minusDays(30);
        LocalDate dueDate = borrowDate.plusDays(28);
        Loan loan = new Loan("L004", book, user, borrowDate, dueDate);

        assertTrue(service.isOverdue(loan));
    }

    @Test
    void testBorrowMediaWithRestrictions() {  // US4.1
        Media media = new Book("Title", "Author", "123");
        User user = new User("U001", "John", "john@example.com");
        Fine unpaidFine = new Fine(100);
        user.addFine(unpaidFine);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.borrowMedia(media, user));
        assertEquals("Cannot borrow: overdue books or unpaid fines", ex.getMessage());  // غيّر إلى ex.getMessage()
    }

    @Test
    void testBorrowMediaPolymorphism() {  // US5.1
        Book book = new Book("Book Title", "Author", "123");
        Loan loan = service.borrowMedia(book, new User("U002", "Jane", "jane@example.com"));
        assertEquals(LocalDate.now().plusDays(28), loan.getDueDate());  // Book 28 days
    }

    @Test
    void testLoanWithMediaPolymorphism() {
        Media book = new Book("Title", "Author", "123");
        User user = new User("U001", "John", "john@example.com");
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(28);
        Loan loan = new Loan("L001", book, user, borrowDate, dueDate);

        assertEquals(book, loan.getMedia());  // Media getter (US5.1)
        assertEquals("Title", loan.getMedia().getTitle());  // Polymorphism access
    }

    @Test
    void testCalculateFineForLoan() {
        Loan loan = new Loan("L001", new Book("Title", "Author", "123"), new User("U001", "John", "john@example.com"), LocalDate.now().minusDays(30), LocalDate.now().minusDays(2));
        service.addLoan(loan);
        int fine = service.calculateFineForLoan(loan);
        assertEquals(20, fine);  // 2 days * 10 NIS book (US5.2)
    }

    @Test
    void testBorrowCD7Days() {
        CD cd = new CD("CD Title", "Artist", "456");
        User user = new User("U001", "John", "john@example.com");
        Loan loan = service.borrowMedia(cd, user);
        assertEquals(LocalDate.now().plusDays(7), loan.getDueDate());  // US5.1
    }

    @Test
    void testBorrowRestrictionsWithFine() {
        User user = new User("U002", "ws", "ws2022@gmail.com");
        Fine unpaidFine = new Fine(50);
        user.addFine(unpaidFine);  // US4.1
        Media media = new Book("Title", "Author", "123");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.borrowMedia(media, user));
        assertEquals("Cannot borrow: overdue books or unpaid fines", ex.getMessage());  // إصلاح: استخدم ex.getMessage() بدلاً من outputStream
    }

    @Test
    void testBookFineStrategy() {
        FineStrategy strategy = new BookFineStrategy();
        assertEquals(50, strategy.calculateFine(5));  // 10*5 = 50 NIS (US5.2)
    }

    @Test
    void testCDFineStrategy() {
        FineStrategy strategy = new CDFineStrategy();
        assertEquals(60, strategy.calculateFine(3));  // 20*3 = 60 NIS (US5.2)
    }
}