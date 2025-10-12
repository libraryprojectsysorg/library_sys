package org.library.Domain;

import org.library.Service.Strategy.fines.FineStrategy;

/**
 * Abstract for media (Sprint 5: polymorphism).
 * @author YourName
 * @version 1.0
 */
public abstract class Media {
    protected final String title, author, isbn;
    protected boolean available = true;

    /**
     * Constructor for Media.
     * @param title the title
     * @param author the author
     * @param isbn the ISBN
     * @throws IllegalArgumentException if invalid details
     */
    public Media(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid media details: title, author, or ISBN cannot be null or empty");
        }
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    /**
     * Loan days per type (US5.1).
     * @return days (e.g., 28 for book, 7 for CD)
     */
    public abstract int getLoanDays();

    /**
     * Fine strategy per type (US5.2).
     * @return FineStrategy
     */
    public abstract FineStrategy getFineStrategy();

    /**
     * Get title.
     * @return title
     */
    public String getTitle() { return title; }

    /**
     * Get author.
     * @return author
     */
    public String getAuthor() { return author; }

    /**
     * Get ISBN.
     * @return ISBN
     */
    public String getIsbn() { return isbn; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}