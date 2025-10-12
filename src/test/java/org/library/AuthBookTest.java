package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.Book;
import org.library.Domain.Media;
import org.library.Service.Strategy.BookService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * Unit tests for AuthBook (US1.3, US1.4).
 * Covers add/search success/failure, edge cases, and mocking for coverage >80%.
 *
 * @author Your Name
 * @version 1.0-SNAPSHOT
 */
@ExtendWith(MockitoExtension.class)  // Extension for class-level mocks
public class AuthBookTest {
    private BookService bookService;

    // Fields for mocks (fixed: @Mock/@InjectMocks on fields, not locals)
    @Mock
    private List<Book> mockBooks;  // Mock the books list

    @InjectMocks
    private BookService mockService;  // Inject mock into service

    @BeforeEach
    public void setUp() {
        bookService = new BookService();
        // Reset mocks if needed
        reset(mockBooks);
    }

    @Test
    public void testAddBook() {
        boolean added = bookService.addBook("Java Book", "Author1", "123456");  // Check return
        assertTrue(added);
        List<Book> results = bookService.searchBooks("Java");
        assertEquals(1, results.size());  // Searchable
        assertTrue(results.get(0).isAvailable());  // Available to borrow
    }

    @Test
    public void testAddDuplicateISBN() {
        bookService.addBook("Book1", "Auth", "111");
        boolean added = bookService.addBook("Book2", "Auth", "111");  // Check return
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
    public void testAddInvalidInput() {  // Covers throw branch
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(null, "Author", "123"));
    }

    // Mocking test (fixed: uses class fields, covers stream branch)
    @Test
    public void testSearchWithMock() {  // Mock no match branch
        when(mockBooks.stream()).thenReturn(Stream.<Book>empty());  // Mock empty stream (inferred type OK)
        List<Book> results = mockService.searchBooks("query");
        assertTrue(results.isEmpty());  // No match
        verify(mockBooks).stream();  // Verify filter call
    }
    @Test
    public void testAddBookDuplicateFalseBranch() {  // Covers false in anyMatch
        bookService.addBook("Book1", "Auth", "111");
        boolean added = bookService.addBook("Book2", "Auth", "111");
        assertFalse(added);  // False branch
    }

    @Test
    public void testSearchNoMatchFullStream() {  // Covers false in all filter conditions
        bookService.addBook("Java Book", "Author", "123");
        List<Book> results = bookService.searchBooks("Python");  // No match in title/author/isbn
        assertTrue(results.isEmpty());  // Full false path in stream
    }

    @Test
    public void testAddBookEmptyTitle() {  // Covers throw in if null/empty
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook("", "Author", "123"));
    }
    @Test
    void testMediaGetters() {
        Media book = new Book("Title", "Author", "123");
        assertEquals("Title", book.getTitle());  // Getter branch
        assertEquals("Author", book.getAuthor());
        assertEquals("123", book.getIsbn());
    }

    @Test
    void testMediaConstructorInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Book(null, "Author", "123"));
        assertTrue(ex.getMessage().contains("Invalid media details"));
    }
}