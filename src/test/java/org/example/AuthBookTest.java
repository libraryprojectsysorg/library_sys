package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit tests for AuthBook (US1.3, US1.4).
 * Covers add/search success/failure, edge cases, and mocking for coverage >80%.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
public class AuthBookTest {
    private AuthBook bookService;

    @BeforeEach
    public void setUp() {
        bookService = new AuthBook();
    }

    @Test
    public void testAddBook() {
        boolean added = bookService.addBook("Java Book", "Author1", "123456");  // Changed to check return
        assertTrue(added);
        List<Book> results = bookService.searchBooks("Java");
        assertEquals(1, results.size());  // Searchable
        assertTrue(results.get(0).isAvailable());  // Available to borrow
    }

    @Test
    public void testAddDuplicateISBN() {
        bookService.addBook("Book1", "Auth", "111");
        boolean added = bookService.addBook("Book2", "Auth", "111");  // Changed to check return
        assertFalse(added);  // Fail without throw
    }

    @Test
    public void testSearchBook() {
        bookService.addBook("Test Book", "Test Author", "789");
        List<Book> results = bookService.searchBooks("Test");
        assertEquals(1, results.size());  // Matching results
    }

    @Test
    public void testSearchNoResults() {
        bookService.addBook("Java Book", "Author", "123");
        List<Book> results = bookService.searchBooks("NonExistent");
        assertTrue(results.isEmpty());  // No match
    }

    // New tests for >80% coverage (empty/null, invalid input)
    @Test
    public void testSearchEmptyQuery() {  // Covers empty branch
        List<Book> results = bookService.searchBooks("");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchNullQuery() {  // Covers null branch
        List<Book> results = bookService.searchBooks(null);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testAddInvalidInput() {  // Covers throw branch in constructor
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(null, "Author", "123"));
    }

    // Mocking test with Mockito (prep for Sprint 3, covers stream branch)
    @ExtendWith(MockitoExtension.class)
    @Test
    public void testSearchWithMock() {  // Mock no match branch
        @Mock List<Book> mockBooks;  // Mock the books list
        @InjectMocks AuthBook mockService = new AuthBook() {  // Inject mock
            { this.books = mockBooks; }
        };

        when(mockBooks.stream()).thenReturn(java.util.stream.Stream.<Book>empty());  // Mock empty stream
        List<Book> results = mockService.searchBooks("query");
        assertTrue(results.isEmpty());  // No match
        verify(mockBooks).stream();  // Verify filter call
    }
}