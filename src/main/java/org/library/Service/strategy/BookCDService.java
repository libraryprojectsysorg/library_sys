package org.library.Service.strategy;
import org.library.domain.Book;
import org.library.domain.CD;
import java.util.List;
import java.util.stream.Collectors;

public class BookCDService {
    private List<Book> books;
    private List<CD> cds;

    public BookCDService() {
        this.books = null;
        this.cds = null;
    }

    public BookCDService(List<Book> books, List<CD> cds) {
        this.books = books;
        this.cds = cds;
    }

    public boolean addBook(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details");
        }

        List<Book> existingBooks = books != null ? books : BookFileHandler.loadAllBooks();
        if (existingBooks.stream().anyMatch(b -> b.getIsbn().equals(isbn))) return false;

        Book newBook = new Book(title, author, isbn);
        if (books != null) books.add(newBook);
        else BookFileHandler.saveBook(newBook);

        return true;
    }

    public List<Book> searchBooks(String query) {
        List<Book> allBooks = books != null ? books : BookFileHandler.loadAllBooks();
        if (query == null || query.isEmpty()) return allBooks;

        String lowerQuery = query.toLowerCase();
        return allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||
                        b.getIsbn().toLowerCase().contains(lowerQuery))
                .toList();
    }

    public boolean removeByIsbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) return false;
        List<Book> allBooks = books != null ? books : BookFileHandler.loadAllBooks();
        boolean removed = allBooks.removeIf(b -> b.getIsbn().equals(isbn));
        if (removed && books == null) BookFileHandler.rewriteAllBooks(allBooks);
        return removed;
    }

    public boolean addCD(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid CD details");
        }

        List<CD> existingCDs = cds != null ? cds : CDFileHandler.loadAllCDs();
        if (existingCDs.stream().anyMatch(c -> c.getIsbn().equals(isbn))) return false;

        CD newCD = new CD(title, author, isbn);
        if (cds != null) cds.add(newCD);
        else CDFileHandler.saveCD(newCD);
        return true;
    }

    public List<CD> searchCD(String query) {
        List<CD> allCDs = cds != null ? cds : CDFileHandler.loadAllCDs();
        if (query == null || query.isEmpty()) return allCDs;
        String lowerQuery = query.toLowerCase();
        return allCDs.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(lowerQuery) ||
                        c.getAuthor().toLowerCase().contains(lowerQuery) ||
                        c.getIsbn().toLowerCase().contains(lowerQuery))
                .toList();
    }

    public boolean removeCDByCode(String isbn) {
        if (isbn == null || isbn.isEmpty()) return false;
        List<CD> allCDs = cds != null ? cds : CDFileHandler.loadAllCDs();
        boolean removed = allCDs.removeIf(c -> c.getIsbn().equals(isbn));
        if (removed && cds == null) CDFileHandler.removeCDByCode(isbn);
        return removed;
    }
}
