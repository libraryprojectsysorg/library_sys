package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for book management (US1.3 Add Book, US1.4 Search Book).
 * Handles adding and searching books with duplicate checks.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class AuthBook {
    private List<Book> books;

    /**
     * Default constructor with empty book list.
     */
    public AuthBook() {
        this.books = new ArrayList<>();  // Default for normal use
    }

    /**
     * Constructor for dependency injection (e.g., mocking in tests).
     *
     * @param books the list of books to use (for mocking)
     */
    public AuthBook(List<Book> books) {
        this.books = books;  // For Mockito inject
    }

    /**
     * Adds a book if ISBN is not duplicate.
     *
     * @param title the book's title
     * @param author the book's author
     * @param isbn the book's ISBN
     * @return true if added successfully, false if duplicate ISBN
     * @throws IllegalArgumentException if title, author, or ISBN is invalid
     */
    public boolean addBook(String title, String author, String isbn) {  // Return boolean for coverage
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details");  // Branch for null/empty (edge case)
        }
        if (books.stream().anyMatch(b -> b.getIsbn().equals(isbn))) {
            return false;  // Duplicate branch (fail without throw for test coverage)
        }
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);  // Searchable and available
        return true;
    }

    /**
     * Searches books by title, author, or ISBN.
     *
     * @param query the search query
     * @return list of matching books (empty if no match or invalid query)
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.isEmpty()) {  // New branch for empty/null (edge case)
            return new ArrayList<>();  // Empty list
        }
        String lowerQuery = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||  // True/false branch 1
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||  // True/false branch 2
                        b.getIsbn().toLowerCase().contains(lowerQuery))  // True/false branch 3
                .collect(Collectors.toList());  // No match → empty (covers false filter)
    }
}