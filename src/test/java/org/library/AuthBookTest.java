package org.library;

import org.junit.jupiter.api.*;
import org.library.Domain.Book;
import org.library.Service.Strategy.BookFileHandler;
import org.library.Service.Strategy.BookService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthBookTest {

    private BookService bookService;
    private static final String TEST_FILE = "test-books.txt";

    @BeforeAll
    void setup() {
        BookFileHandler.setBooksFile("test-books.txt"); // نوجه الملف للاختبارات
        bookService = new BookService();
    }

    @BeforeEach
    void cleanFile() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testAddBook() {
        boolean added = bookService.addBook("Java Basics", "Weam Ahmad", "ISBN123");
        assertTrue(added, "الكتاب يجب أن يضاف بنجاح");

        // نتحقق من أن الكتاب موجود فعلاً
        List<Book> books = bookService.searchBooks("ISBN123");
        assertEquals(1, books.size());
        assertEquals("Java Basics", books.get(0).getTitle());
    }

    @Test
    void testRemoveBook() {
        bookService.addBook("Python Basics", "Seba", "ISBN999");
        boolean removed = bookService.removeByIsbn("ISBN999");
        assertTrue(removed, "الكتاب يجب أن يُحذف");

        // يجب ألا يبقى الكتاب في الملف
        List<Book> books = bookService.searchBooks("ISBN999");
        assertTrue(books.isEmpty());
    }

    @Test
    void testSearchBook() {
        bookService.addBook("C++ Basics", "Weam", "ISBN555");
        List<Book> results = bookService.searchBooks("C++");
        assertFalse(results.isEmpty(), "يجب العثور على الكتاب");
        assertEquals("C++ Basics", results.get(0).getTitle());
    }

    // ---------------------------------------
    // نستخدم طريقة لتغيير مسار الملف في BookFileHandler
    // ---------------------------------------
    private void replaceFileHandlerPath() {
        try {
            java.lang.reflect.Field field = BookFileHandler.class.getDeclaredField("BOOKS_FILE");
            field.setAccessible(true);
            field.set(null, TEST_FILE); // نوجهه لملف الاختبار
        } catch (Exception e) {
            throw new RuntimeException("❌ فشل إعادة توجيه ملف الاختبار", e);
        }
    }
}
