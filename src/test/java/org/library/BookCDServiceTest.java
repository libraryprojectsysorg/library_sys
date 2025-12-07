package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.library.Domain.Book;
import org.library.Domain.CD;
import org.library.Service.Strategy.BookCDService;
import org.library.Service.Strategy.BookFileHandler;
import org.library.Service.Strategy.CDFileHandler;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookCDServiceTest {

    private BookCDService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {

        BookFileHandler.setBooksFile(tempDir.resolve("books.txt").toString());
        CDFileHandler.setCdsFile(tempDir.resolve("cds.txt").toString());

        service = new BookCDService();
    }

    @Test
    void addBook_ShouldAddSuccessfully() {
        boolean added = service.addBook("Clean Code", "Robert C. Martin", "978-0132350884");
        assertTrue(added);

        List<Book> books = service.searchBooks("");
        assertEquals(1, books.size());
        assertEquals("Clean Code", books.get(0).getTitle());
    }

    @Test
    void addBook_Duplicate_ShouldReturnFalse() {
        service.addBook("1984", "George Orwell", "12345");

        boolean addedAgain = service.addBook("1984", "George Orwell", "12345");
        assertFalse(addedAgain);
    }

    @Test
    void addBook_InvalidData_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addBook("", "Author", "123"));
        assertThrows(IllegalArgumentException.class, () ->
                service.addBook(null, "Author", "123"));
    }

    @Test
    void removeBook_ShouldRemoveSuccessfully() {
        service.addBook("Test Book", "Test Author", "99999");
        boolean removed = service.removeByIsbn("99999");
        assertTrue(removed);

        assertTrue(service.searchBooks("").isEmpty());
    }

    @Test
    void searchBooks_ShouldFindByTitleAuthorOrIsbn() {
        service.addBook("Java Guide", "Ahmed", "111");
        service.addBook("Python Book", "Ali", "222");

        List<Book> results = service.searchBooks("java");
        assertEquals(1, results.size());

        results = service.searchBooks("222");
        assertEquals(1, results.size());
    }

    @Test
    void addCD_ShouldAddSuccessfully() {
        boolean added = service.addCD("Thriller", "Michael Jackson", "CD001");
        assertTrue(added);
    }

    @Test
    void addCD_Duplicate_ShouldReturnFalse() {
        service.addCD("Album", "Artist", "XYZ");
        boolean added = service.addCD("Album", "Artist", "XYZ");
        assertFalse(added);
    }

    @Test
    void searchCD_ShouldWork() {
        service.addCD("Back in Black", "AC/DC", "CD888");
        List<CD> results = service.searchCD("black");
        assertEquals(1, results.size());
    }

    @Test
    void removeCD_ShouldRemove() {
        service.addCD("Test CD", "Artist", "DEL123");
        boolean removed = service.removeCDByCode("DEL123");
        assertTrue(removed);
    }
}