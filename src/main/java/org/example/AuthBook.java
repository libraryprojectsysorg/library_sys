package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AuthBook {
    private List<Book> books = new ArrayList<>();


    public boolean addBook(String title, String author, String isbn) {  // Changed to boolean for coverage
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details");  // Branch for null/empty
        }
        if (books.stream().anyMatch(b -> b.getIsbn().equals(isbn))) {
            return false;  // Duplicate branch (changed from throw for test)
        }
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);  // Searchable and available
        return true;
    }


    public List<Book> searchBooks(String query) {
        if (query == null || query.isEmpty()) {  // New branch for empty/null
            return new ArrayList<>();  // Empty list
        }
        String lowerQuery = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||
                        b.getIsbn().toLowerCase().contains(lowerQuery))  // Filter branches: true/false
                .collect(Collectors.toList());  // No match → empty
    }
}