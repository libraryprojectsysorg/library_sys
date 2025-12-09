package org.library;

import org.junit.jupiter.api.*;
import org.library.domain.Book;
import org.library.Service.strategy.BookFileHandler;
import org.library.Service.strategy.BookCDService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
 class AuthBookTest {

    private BookCDService bookCDService;
    private static final String TEST_FILE = "test-books.txt";

    @BeforeAll
    void setup() {

        BookFileHandler.setBooksFile(TEST_FILE);
        bookCDService = new BookCDService();
    }

    @BeforeEach
    void cleanFile() {

        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }


    @Test
    void testAddBookToEmptyFile() {

        boolean added = bookCDService.addBook("EmptyFileBook", "Author", "ISBN100");
        assertTrue(added, "إضافة كتاب في ملف فارغ يجب أن تنجح");
    }

    @Test
    void testAddBookUniqueIsbn() {

        boolean added1 = bookCDService.addBook("Soft", "Author1", "ISBN001");
        assertTrue(added1);

        boolean added2 = bookCDService.addBook("Soft", "Author2", "ISBN002");
        assertTrue(added2, "نفس الاسم ولكن ISBN مختلف يجب أن يُضاف");


        boolean added3 = bookCDService.addBook("Soft", "Author3", "ISBN001");
        assertFalse(added3, "كتاب بنفس ISBN يجب ألا يُضاف");


        List<Book> books = bookCDService.searchBooks("Soft");
        assertEquals(2, books.size());
    }


    @Test
    void testRemoveBookFromEmptyFile() {

        boolean removed = bookCDService.removeByIsbn("ISBN_NOT_EXIST");
        assertFalse(removed);
    }

    @Test
    void testRemoveBook() {
        bookCDService.addBook("Python Basics", "Seba", "ISBN999");
        boolean removed = bookCDService.removeByIsbn("ISBN999");
        assertTrue(removed);

        List<Book> books = bookCDService.searchBooks("Python");
        assertTrue(books.isEmpty());
    }

    @Test
    void testRemoveNonExistingBook() {
        boolean removed = bookCDService.removeByIsbn("NON_EXISTENT_ISBN");
        assertFalse(removed);
    }


    @Test
    void testSearchBook() {
        bookCDService.addBook("C++ Basics", "Weam", "ISBN555");
        List<Book> results = bookCDService.searchBooks("C++");
        assertFalse(results.isEmpty());
        assertEquals("C++ Basics", results.get(0).getTitle());
    }

    @Test
    void testSearchNonExistingBook() {
        List<Book> results = bookCDService.searchBooks("NonExisting");
        assertTrue(results.isEmpty());
    }


    @Test
    void testBookLoanDaysAndFineRate() {
        Book book = new Book("Java Basics", "Author", "ISBN101");

        assertEquals(28, book.getLoanDays());
        assertEquals(0, book.getDailyFineRate("SUPER_ADMIN"));
        assertEquals(5, book.getDailyFineRate("LIBRARIAN"));
        assertEquals(5, book.getDailyFineRate("ADMIN"));
        assertEquals(10, book.getDailyFineRate("STUDENT"));
    }

    @Test
    void testBookFineStrategy() {
        Book book = new Book("Java Basics", "Author", "ISBN102");
        assertNotNull(book.getFineStrategy());
    }


    @Test
    void testRewriteAllBooks() {
        Book b1 = new Book("Book1", "Author1", "ISBN1");
        Book b2 = new Book("Book2", "Author2", "ISBN2");

        BookFileHandler.saveBook(b1);
        BookFileHandler.saveBook(b2);


        BookFileHandler.rewriteAllBooks(List.of(b1));

        List<Book> books = BookFileHandler.loadAllBooks();
        assertEquals(1, books.size());
        assertEquals("Book1", books.get(0).getTitle());
    }

    @Test
    void testLoadFromEmptyFile() {
        List<Book> books = BookFileHandler.loadAllBooks();
        assertTrue(books.isEmpty());
    }
}
