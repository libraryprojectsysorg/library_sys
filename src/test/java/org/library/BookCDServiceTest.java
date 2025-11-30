package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.Book;
import org.library.Service.Strategy.BookCDService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookCDServiceTest {

    private BookCDService service;
    private List<Book> testBooks;

    @BeforeEach
    void setUp() {

        testBooks = new ArrayList<>();


        testBooks.add(new Book("Java Programming", "John Doe", "111"));
        testBooks.add(new Book("Clean Code", "Robert Martin", "222"));
        testBooks.add(new Book("Advanced Java", "Jane Smith", "333"));


        service = new BookCDService(testBooks);
    }



    @Test
    void shouldReturnAllBooks_WhenQueryIsEmpty() {
        List<Book> result = service.searchBooks("");
        assertEquals(3, result.size(), "يجب أن يعيد كل الكتب إذا كان البحث فارغاً");
    }

    @Test
    void shouldSearchByTitle_CaseInsensitive() {

        List<Book> result = service.searchBooks("java");

        assertEquals(2, result.size(), "يجب أن يجد كتابين يحتويان على كلمة Java");
        assertTrue(result.stream().anyMatch(b -> b.getIsbn().equals("111")));
        assertTrue(result.stream().anyMatch(b -> b.getIsbn().equals("333")));
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
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyList_WhenNoMatchFound() {
        List<Book> result = service.searchBooks("NonExistentBook");
        assertTrue(result.isEmpty(), "يجب أن تعود القائمة فارغة إذا لم يتم العثور على شيء");
    }


    @Test
    void shouldAddBookSuccessfully_WhenValidAndUnique() {

        boolean added = service.addBook("New Book", "New Author", "444");

        assertTrue(added, "يجب أن تتم الإضافة بنجاح");
        assertEquals(4, testBooks.size(), "عدد الكتب في القائمة يجب أن يزداد");
        assertEquals("New Book", testBooks.get(3).getTitle());
    }

    @Test
    void shouldFailToAdd_WhenIsbnAlreadyExists() {

        boolean added = service.addBook("Duplicate Book", "Author", "111");

        assertFalse(added, "لا يجب السماح بإضافة كتاب مكرر الـ ISBN");
        assertEquals(3, testBooks.size(), "يجب ألا يتغير عدد الكتب");
    }

    @Test
    void shouldThrowException_WhenInputsAreInvalid() {

        assertThrows(IllegalArgumentException.class, () -> {
            service.addBook("", "Author", "123");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.addBook(null, "Author", "123");
        });
    }



    @Test
    void shouldRemoveBook_WhenIsbnExists() {

        boolean removed = service.removeByIsbn("111");

        assertTrue(removed, "يجب أن تنجح عملية الحذف");
        assertEquals(2, testBooks.size(), "يجب أن ينقص عدد الكتب واحداً");

        assertTrue(service.searchBooks("111").isEmpty());
    }

    @Test
    void shouldReturnFalse_WhenRemovingNonExistentIsbn() {
        boolean removed = service.removeByIsbn("99999");
        assertFalse(removed, "يجب أن تعود false عند محاولة حذف كتاب غير موجود");
        assertEquals(3, testBooks.size());
    }
}