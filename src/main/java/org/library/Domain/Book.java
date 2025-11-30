package org.library.Domain;

import org.library.Service.Strategy.fines.BookFineStrategy;
import org.library.Service.Strategy.fines.FineStrategy;

/**
 * Entity for book (Sprint 1 & 5: extends Media for polymorphism).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public class Book extends Media {
    /**
     * Constructor for Book (US1.3).
     * @param title the book title
     * @param author the book author
     * @param isbn the ISBN
     * @throws IllegalArgumentException if invalid details
     */
    public Book(String title, String author, String isbn) {
        super(title, author, isbn);
    }

    /**
     * Loan days for book (US5.1 polymorphism).
     * @return 28 days
     */
    @Override
    public int getLoanDays() {
        return 28;
    }

    /**
     * Fine strategy for book (US5.2 Strategy Pattern).
     * @return BookFineStrategy
     */
    @Override
    public FineStrategy getFineStrategy()
    {
        return new BookFineStrategy();
    }

    @Override
    public int getDailyFineRate(String userRole) {
        if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) return 0;
        if ("LIBRARIAN".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole)) return 5;
        return 10;
    }


}
