package org.library;

import org.junit.jupiter.api.Test;
import org.library.domain.Book;
import org.library.domain.Media;
import org.library.Service.strategy.fines.BookFineStrategy;
import org.library.Service.strategy.fines.FineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void shouldCreateBookWithValidDetails() {
        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884");

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("978-0132350884", book.getIsbn());
        assertTrue(book.isAvailable()); // من Media
    }

    @Test
    void shouldThrowException_WhenTitleIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Book(null, "Author", "123"));
    }

    @Test
    void shouldThrowException_WhenIsbnIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Book("Title", "Author", ""));
    }

    @Test
    void getLoanDays_ShouldReturn28() {
        Book book = new Book("Java", "Ahmad", "111");

        assertEquals(28, book.getLoanDays());
    }

    @Test
    void getFineStrategy_ShouldReturnBookFineStrategyInstance() {
        Book book = new Book("Test", "Test", "TEST123");

        FineStrategy strategy = book.getFineStrategy();

        assertNotNull(strategy);
        assertTrue(strategy instanceof BookFineStrategy);
    }

    @Test
    void getDailyFineRate_ShouldReturn0_ForSuperAdmin() {
        Book book = new Book("Book", "Author", "123");

        assertEquals(0, book.getDailyFineRate("SUPER_ADMIN"));
        assertEquals(0, book.getDailyFineRate("super_admin"));
    }

    @Test
    void getDailyFineRate_ShouldReturn5_ForAdminOrLibrarian() {
        Book book = new Book("Book", "Author", "123");

        assertEquals(5, book.getDailyFineRate("ADMIN"));
        assertEquals(5, book.getDailyFineRate("admin"));
        assertEquals(5, book.getDailyFineRate("LIBRARIAN"));
        assertEquals(5, book.getDailyFineRate("librarian"));
    }

    @Test
    void getDailyFineRate_ShouldReturn10_ForRegularUser() {
        Book book = new Book("Book", "Author", "123");

        assertEquals(10, book.getDailyFineRate("USER"));
        assertEquals(10, book.getDailyFineRate("student"));
        assertEquals(10, book.getDailyFineRate(null)); // أي role غير معروف = 10
    }

    @Test
    void shouldInheritFromMedia() {
        Book book = new Book("Title", "Author", "ISBN");

        assertTrue(book instanceof Media);
    }
}