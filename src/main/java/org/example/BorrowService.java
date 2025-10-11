package org.example;  // Package نفسه

import java.time.LocalDate;  // Import للـ dates

/**
 * Service for borrowing and overdue detection (US2.1, US2.2).
 * Uses Factory pattern for Loan creation.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class BorrowService {  // Public class، Service layer
    /**
     * Factory method to create Loan for borrowing.
     *
     * @param book the book to borrow
     * @return Loan if successful, null if unavailable
     */
    public Loan borrowBook(Book book) {  // Method للـ US2.1: borrow
        if (!book.isAvailable()) {  // Check if available (from Sprint 1)
            return null;  // Block if not available (branch for false)
        }
        Loan loan = new Loan(book, LocalDate.now().plusDays(28));  // Create Loan with due = today +28 days
        book.setAvailable(false);  // Mark as borrowed (update Book from Sprint 1)
        return loan;  // Return Loan
    }

    /**
     * Detects if loan is overdue (>28 days).
     *
     * @param loan the loan to check
     * @return true if overdue
     */
    public boolean isOverdue(Loan loan) {  // Method للـ US2.2: detect overdue
        return LocalDate.now().isAfter(loan.getDueDate().plusDays(28));  // Check if today > due +28 days
    }
}