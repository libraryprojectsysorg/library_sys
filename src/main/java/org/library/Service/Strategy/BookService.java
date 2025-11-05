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

    private List<Book> books;


    public BookService() {
        this.books = null;  // null يعني استخدم الملفات
    }

    // كونستركتور للاختبارات: يسمح بتمرير قائمة موكد
    public BookService(List<Book> books) {
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
            books.add(newBook);  // تحديث قائمة الاختبار
        } else {
            BookFileHandler.saveBook(newBook);  // تحديث الملفات
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
}
