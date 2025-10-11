package org.example;  // Package نفسه

import org.junit.jupiter.api.Test;  // JUnit 5 import
import org.mockito.MockedStatic;  // Mockito for static mock
import static org.junit.jupiter.api.Assertions.*;  // Asserts
import static org.mockito.Mockito.*;  // Mock methods
import java.time.Clock;  // For mock time
import java.time.LocalDate;  // Dates
import java.time.ZoneId;  // Time zone for mock

/**
 * Tests for BorrowService (US2.1, US2.2).
 * Uses Mockito for time mocking in overdue detection.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class BorrowTest {  // Test class
    private BorrowService service = new BorrowService();  // Instance for tests

    @Test
    public void testBorrowBookSuccess() {  // Test US2.1 success
        Book book = new Book("Title", "Author", "123");  // Book from Sprint 1
        Loan loan = service.borrowBook(book);  // Call method
        assertNotNull(loan);  // Check Loan created
        assertFalse(book.isAvailable());  // Check marked borrowed
        assertEquals(LocalDate.now().plusDays(28), loan.getDueDate());  // Check due +28 days
    }

    @Test
    public void testBorrowUnavailableBook() {  // Test edge case (unavailable)
        Book book = new Book("Title", "Author", "123");
        book.setAvailable(false);  // Set unavailable
        Loan loan = service.borrowBook(book);  // Call method
        assertNull(loan);  // Check return null (block)
    }

    @Test
    public void testOverdueDetectionTrue() {  // Test US2.2 overdue true
        Loan loan = new Loan(new Book("Title", "Author", "123"), LocalDate.now().minusDays(30));  // Due 30 days ago
        assertTrue(service.isOverdue(loan));  // Check true (>28 days)
    }

    @Test
    public void testOverdueDetectionFalse() {  // Test not overdue
        Loan loan = new Loan(new Book("Title", "Author", "123"), LocalDate.now().plusDays(1));  // Due tomorrow
        assertFalse(service.isOverdue(loan));  // Check false
    }

    @Test
    public void testOverdueWithMockTime() {  // Test with Mockito mock time (spec requirement)
        LocalDate borrowDate = LocalDate.of(2025, 10, 7);  // Borrow date (e.g., today in test scenario)
        LocalDate mockToday = borrowDate.plusDays(29);  // Mock advanced date (29+ days, after due date)
        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class)) {  // Mock static LocalDate.now()
            mockedDate.when(() -> LocalDate.now()).thenReturn(mockToday);  // Mock return mockToday
            Loan loan = new Loan(new Book("Title", "Author", "123"), borrowDate);  // Borrow on borrowDate (due = borrowDate + 28)
            assertTrue(service.isOverdue(loan));  // Check overdue with mock (current > due)
        }  // Try-with-resources: clean up mock
    }
}