package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.Book;
import org.library.Service.Strategy.BookCDService;
import org.library.Service.Strategy.BookFileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookCDServiceTest {

    private BookCDService service;
    private List<Book> testBooks;
    private static final String TEST_FILE = "books.txt";

    @BeforeEach
    void setUp() {
        testBooks = new ArrayList<>();
        testBooks.add(new Book("Java Programming", "John Doe", "111"));
        testBooks.add(new Book("Clean Code", "Robert Martin", "222"));
        testBooks.add(new Book("Advanced Java", "Jane Smith", "333"));

        service = new BookCDService(testBooks, null);



        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        BookFileHandler.setBooksFile(TEST_FILE);
    }


    @Test
    void shouldAddBook_WhenValidAndBooksNotNull() {
        boolean added = service.addBook("New Book", "New Author", "444");
        assertTrue(added);
        assertEquals(4, testBooks.size());
    }

    @Test
    void shouldFailAddBook_WhenIsbnAlreadyExists() {
        boolean added = service.addBook("Duplicate Book", "Author", "111");
        assertFalse(added);
        assertEquals(3, testBooks.size());
    }

    @Test
    void shouldAllowSameTitleDifferentIsbn() {
        boolean added = service.addBook("Java Programming", "John Doe", "555");
        assertTrue(added);
        assertEquals(4, testBooks.size());
    }

    @Test
    void shouldThrowException_WhenInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> service.addBook("", "Author", "123"));
        assertThrows(IllegalArgumentException.class, () -> service.addBook(null, "Author", "123"));
        assertThrows(IllegalArgumentException.class, () -> service.addBook("Title", "", "123"));
        assertThrows(IllegalArgumentException.class, () -> service.addBook("Title", null, "123"));
        assertThrows(IllegalArgumentException.class, () -> service.addBook("Title", "Author", ""));
        assertThrows(IllegalArgumentException.class, () -> service.addBook("Title", "Author", null));
    }

    @Test
    void shouldAddBook_WhenBooksNull() {
        BookCDService nullService = new BookCDService(null,null);
        boolean added = nullService.addBook("File Book", "File Author", "999");
        assertTrue(added);

        List<Book> loadedBooks = BookFileHandler.loadAllBooks();
        assertTrue(loadedBooks.stream().anyMatch(b -> b.getIsbn().equals("999")));
    }

    @Test
    void shouldFailAddBook_WhenIsbnExistsInFile() {
        BookCDService nullService = new BookCDService(null,null);
        nullService.addBook("Book1", "Author1", "AAA");
        boolean added = nullService.addBook("Book2", "Author2", "AAA"); // ISBN مكرر
        assertFalse(added);
    }


    @Test
    void shouldRemoveBook_WhenIsbnExists() {
        boolean removed = service.removeByIsbn("111");
        assertTrue(removed);
        assertEquals(2, testBooks.size());
    }

    @Test
    void shouldReturnFalse_WhenRemovingNonExistentIsbn() {
        boolean removed = service.removeByIsbn("999");
        assertFalse(removed);
        assertEquals(3, testBooks.size());
    }

    @Test
    void shouldReturnFalse_WhenIsbnNullOrEmpty() {
        assertFalse(service.removeByIsbn(null));
        assertFalse(service.removeByIsbn(""));
    }

    @Test
    void shouldRemoveBook_WhenBooksNull() {
        BookCDService nullService = new BookCDService(null,null);
        nullService.addBook("Book1", "Author1", "AAA");
        boolean removed = nullService.removeByIsbn("AAA");
        assertTrue(removed);

        List<Book> loadedBooks = BookFileHandler.loadAllBooks();
        assertTrue(loadedBooks.isEmpty());
    }

    @Test
    void shouldReturnAllBooks_WhenQueryIsEmpty() {
        List<Book> result = service.searchBooks("");
        assertEquals(3, result.size());
    }

    @Test
    void shouldReturnAllBooks_WhenQueryIsNull() {
        List<Book> result = service.searchBooks(null);
        assertEquals(testBooks.size(), result.size());
    }

    @Test
    void shouldSearchByTitleCaseInsensitive() {
        List<Book> result = service.searchBooks("java");
        assertEquals(2, result.size());
    }

    @Test
    void shouldSearchByAuthor() {
        List<Book> result = service.searchBooks("Robert");
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void shouldSearchByIsbn() {
        List<Book> result = service.searchBooks("222");
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmpty_WhenNoMatch() {
        List<Book> result = service.searchBooks("NonExistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSearchBooks_WhenBooksNull() {
        BookCDService nullService = new BookCDService(null,null);
        nullService.addBook("Book1", "Author1", "AAA");
        List<Book> result = nullService.searchBooks("Book1");
        assertEquals(1, result.size());
    }
}
