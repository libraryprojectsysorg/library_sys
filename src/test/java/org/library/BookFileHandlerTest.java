package org.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.Book;
import org.library.Service.Strategy.BookFileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookFileHandlerTest {


    private static final String TEST_FILE_PATH = "test_books_db.txt";

    @BeforeEach
    void setUp() {

        BookFileHandler.setBooksFile(TEST_FILE_PATH);


        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterEach
    void tearDown() {

        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldSaveAndLoadBookCorrectly() {

        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884");


        BookFileHandler.saveBook(book);
        List<Book> loadedBooks = BookFileHandler.loadAllBooks();


        assertNotNull(loadedBooks);
        assertEquals(1, loadedBooks.size(), "يجب أن يحتوي الملف على كتاب واحد");

        Book loadedBook = loadedBooks.get(0);
        assertEquals("Clean Code", loadedBook.getTitle());
        assertEquals("Robert Martin", loadedBook.getAuthor());
        assertEquals("978-0132350884", loadedBook.getIsbn());
    }

    @Test
    void shouldRewriteAllBooksCorrectly() {

        List<Book> books = new ArrayList<>();
        books.add(new Book("Book 1", "Author 1", "ISBN1"));
        books.add(new Book("Book 2", "Author 2", "ISBN2"));


        BookFileHandler.rewriteAllBooks(books);


        List<Book> loadedBooks = BookFileHandler.loadAllBooks();


        assertEquals(2, loadedBooks.size());
        assertEquals("ISBN1", loadedBooks.get(0).getIsbn());
        assertEquals("ISBN2", loadedBooks.get(1).getIsbn());
    }

    @Test
    void shouldHandleEmptyFileGracefully() {

        List<Book> loadedBooks = BookFileHandler.loadAllBooks();


        assertNotNull(loadedBooks, "يجب أن ترجع القائمة فارغة وليست null");
        assertTrue(loadedBooks.isEmpty(), "القائمة يجب أن تكون فارغة");
    }
}