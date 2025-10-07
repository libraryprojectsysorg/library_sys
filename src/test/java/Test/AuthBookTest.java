package Test;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AuthBookTest {
    private AuthBook bookService;

    @BeforeEach
    public void setUp() {
        bookService = new AuthBook();
    }

    @Test
    public void testAddBook() {
        bookService.addBook("Java Book", "Author1", "123456");
        List<Book> results = bookService.searchBooks("Java");
        assertEquals(1, results.size());  // Searchable
        assertTrue(results.get(0).isAvailable());  // Available to borrow
    }

    @Test
    public void testAddDuplicateISBN() {
        bookService.addBook("Book1", "Auth", "111");
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook("Book2", "Auth", "111"));
    }

    @Test
    public void testSearchBook() {
        bookService.addBook("Test Book", "Test Author", "789");
        List<Book> results = bookService.searchBooks("Test");
        assertEquals(1, results.size());  // Matching results
    }

    @Test
    public void testSearchNoResults() {
        List<Book> results = bookService.searchBooks("NonExistent");
        assertTrue(results.isEmpty());
    }
}