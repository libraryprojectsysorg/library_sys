/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */



package org.library.Service.Strategy;

import org.library.Domain.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {

    public BookService() { }

    public boolean addBook(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details");
        }

        List<Book> existingBooks = BookFileHandler.loadAllBooks();
        if (existingBooks.stream().anyMatch(b -> b.getIsbn().equals(isbn))) {
            return false;
        }

        Book newBook = new Book(title, author, isbn);
        BookFileHandler.saveBook(newBook);
        return true;
    }

    /** دالة بحث عامة تستخدم للبحث ولجلب الكتاب من الـ ISBN */
    public List<Book> searchBooks(String query) {
        if (query == null) {
            return new ArrayList<>();
        }

        List<Book> allBooks = BookFileHandler.loadAllBooks();

        if (query.isEmpty()) {
            return allBooks;
        }

        String lowerQuery = query.toLowerCase();

        return allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||
                        b.getIsbn().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public boolean removeByIsbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) return false;

        List<Book> allBooks = BookFileHandler.loadAllBooks();

        boolean removed = allBooks.removeIf(b -> b.getIsbn().equals(isbn));

        if (removed) {
            BookFileHandler.rewriteAllBooks(allBooks);
        }

        return removed;
    }
}