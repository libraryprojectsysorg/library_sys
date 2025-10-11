package org.example;

import java.time.LocalDate;

/**
 * Domain entity for Loan (US2.1).
 * Represents a borrowed book with due date.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class Loan {
    private Book book;
    private LocalDate dueDate;

    /**
     * Constructs a Loan.
     *
     * @param book the borrowed book
     * @param dueDate the due date
     */
    public Loan(Book book, LocalDate dueDate) {
        this.book = book;
        this.dueDate = dueDate;
    }

    /**
     * Gets the borrowed book.
     * @return the book
     */
    public Book getBook() { return book; }

    /**
     * Gets the due date.
     * @return the due date
     */
    public LocalDate getDueDate() { return dueDate; }
}