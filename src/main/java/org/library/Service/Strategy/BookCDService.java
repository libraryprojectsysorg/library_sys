/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */



package org.library.Service.Strategy;

import org.library.Domain.Book;
import org.library.Domain.CD;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookCDService {

    private List<Book> books;
    private List<CD> cd;

    public BookCDService() {
        this.books = null;
    }


    public BookCDService(List<Book> books) {
        this.books = books;
    }

    public boolean addBook(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details");
        }

        List<Book> existingBooks = books != null ? books : BookFileHandler.loadAllBooks();

        if (existingBooks.stream().anyMatch(b -> b.getIsbn().equals(isbn))) {
            return false;
        }

        Book newBook = new Book(title, author, isbn);

        if (books != null) {
            books.add(newBook);
        } else {
            BookFileHandler.saveBook(newBook);
        }

        return true;
    }

    public List<Book> searchBooks(String query) {
        List<Book> allBooks = books != null ? books : BookFileHandler.loadAllBooks();

        if (query == null) return new ArrayList<>();
        if (query.isEmpty()) return allBooks;

        String lowerQuery = query.toLowerCase();
        return allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||
                        b.getIsbn().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public boolean removeByIsbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) return false;

        List<Book> allBooks = books != null ? books : BookFileHandler.loadAllBooks();

        boolean removed = allBooks.removeIf(b -> b.getIsbn().equals(isbn));

        if (removed && books == null) {
            BookFileHandler.rewriteAllBooks(allBooks);
        }

        return removed;
    }

    public List<CD> searchCD(String query) {
        List<CD> allcds = cd != null ? cd : CDFileHandler.loadAllCDs();

        if (query == null) return new ArrayList<>();
        if (query.isEmpty()) return allcds;

        String lQuery = query.toLowerCase();
        return allcds.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lQuery) ||
                        b.getAuthor().toLowerCase().contains(lQuery) ||
                        b.getIsbn().toLowerCase().contains(lQuery))
                .collect(Collectors.toList());
    }

}
