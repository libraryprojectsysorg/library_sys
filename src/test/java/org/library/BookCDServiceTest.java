package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.library.Domain.Book;

import org.library.Domain.CD;
import org.library.Service.Strategy.BookCDService;
import org.library.Service.Strategy.BookFileHandler;
import org.library.Service.Strategy.CDFileHandler;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;

class BookCDServiceTest {

    private BookCDService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        // ضبط مسار الملفات المؤقتة عشان الـ FileHandler يشتغل صح
        BookFileHandler.setBooksFile(tempDir.resolve("books.txt").toString());
        CDFileHandler.setCdsFile(tempDir.resolve("cds.txt").toString());

        // التعديل المهم: نستخدم القنصر اللي بياخد الليستات (in-memory mode)
        // عشان نغطي كل الـ branches اللي فيها books != null و cds != null
        service = new BookCDService(new ArrayList<>(), new ArrayList<>());
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

                service.addBook("Title", null,  "123"));

        assertThrows(IllegalArgumentException.class, () ->
                service.addBook("Title", "Author", null));
    }

    @Test
    void removeBook_ShouldRemoveSuccessfully() {
        service.addBook("Test Book", "Test Author", "99999");

        boolean removed = service.removeByIsbn("99999");
        assertTrue(removed);

        assertTrue(service.searchBooks("").isEmpty());
    }

    @Test
    void removeBook_NonExisting_ShouldReturnFalse() {
        boolean removed = service.removeByIsbn("non-existing-isbn");
        assertFalse(removed);
    }

    @Test
    void searchBooks_ShouldReturnAllWhenQueryEmptyOrNull() {
        service.addBook("Java Guide", "Ahmed", "111");
        service.addBook("Python Book", "Ali", "222");

        assertEquals(2, service.searchBooks("").size());
        assertEquals(2, service.searchBooks(null).size());
    }

    @Test
    void searchBooks_ShouldFindByTitleAuthorOrIsbn() {
        service.addBook("Java Guide", "Ahmed", "111");
        service.addBook("Python Book", "Ali", "222");

        assertEquals(1, service.searchBooks("java").size());
        assertEquals(1, service.searchBooks("AHMED").size());
        assertEquals(1, service.searchBooks("222").size());
    }

    @Test
    void addCD_ShouldAddSuccessfully() {
        boolean added = service.addCD("Thriller", "Michael Jackson", "CD001");
        assertTrue(added);

        assertEquals(1, service.searchCD("").size());
    }

    @Test
    void addCD_Duplicate_ShouldReturnFalse() {
        service.addCD("Album", "Artist", "XYZ");

        boolean added = service.addCD("Album", "Artist", "XYZ");
        assertFalse(added);
    }

    @Test
    void addCD_InvalidData_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addCD("", "Artist", "CD123"));

        assertThrows(IllegalArgumentException.class, () ->
                service.addCD("Title", null, "CD123"));
    }

    @Test
    void searchCD_ShouldWork_WithDifferentCases() {
        service.addCD("Back in Black", "AC/DC", "CD888");

        assertEquals(1, service.searchCD("black").size());
        assertEquals(1, service.searchCD("AC/DC").size());
        assertEquals(1, service.searchCD("cd888").size());
    }

    @Test
    void searchCD_EmptyQuery_ShouldReturnAll() {
        service.addCD("CD1", "Art1", "001");
        service.addCD("CD2", "Art2", "002");

        assertEquals(2, service.searchCD("").size());
        assertEquals(2, service.searchCD(null).size());
    }

    @Test
    void removeCD_ShouldRemoveSuccessfully() {
        service.addCD("Test CD", "Artist", "DEL123");

        boolean removed = service.removeCDByCode("DEL123");
        assertTrue(removed);

        assertTrue(service.searchCD("").isEmpty());
    }

    @Test
    void removeCD_NonExisting_ShouldReturnFalse() {
        boolean removed = service.removeCDByCode("unknown");
        assertFalse(removed);
    }
    @Test
    void constructor_WithInjectedLists_ShouldUseThemInsteadOfFiles() {
        List<Book> books = new ArrayList<>();
        List<CD> cds = new ArrayList<>();

        BookCDService service = new BookCDService(books, cds);

        service.addBook("Java", "Ahmad", "111");
        service.addCD("Hits", "Artist", "CD111");

        assertEquals(1, books.size());
        assertEquals(1, cds.size());
    }
    @Test
    void addBook_ShouldUseInjectedList_WhenProvided() {
        List<Book> injectedBooks = new ArrayList<>();
        List<CD> injectedCds = new ArrayList<>();

        BookCDService service = new BookCDService(injectedBooks, injectedCds);

        service.addBook("Java Book", "Ahmad", "ISBN123");

        assertEquals(1, injectedBooks.size());
        assertEquals("Java Book", injectedBooks.get(0).getTitle());
    }
    @Test
    void shouldUseFileHandlers_WhenListsAreNull() {
        BookCDService service = new BookCDService(); // books = null, cds = null

        try (MockedStatic<BookFileHandler> bookMock = mockStatic(BookFileHandler.class);
             MockedStatic<CDFileHandler> cdMock = mockStatic(CDFileHandler.class)) {
            bookMock.when(BookFileHandler::loadAllBooks).thenReturn(new ArrayList<>());
            cdMock.when(CDFileHandler::loadAllCDs).thenReturn(new ArrayList<>());
            bookMock.when(() -> BookFileHandler.saveBook(any(Book.class))).thenAnswer(i -> null);
            bookMock.when(() -> BookFileHandler.rewriteAllBooks(anyList())).thenAnswer(i -> null);
            cdMock.when(() -> CDFileHandler.saveCD(any(CD.class))).thenAnswer(i -> null);
            cdMock.when(() -> CDFileHandler.removeCDByCode(anyString())).thenAnswer(i -> null);
            assertTrue(service.addBook("Book1", "Author1", "B001"));
            assertTrue(service.addCD("CD1", "Artist1", "C001"));
            assertFalse(service.removeByIsbn("B001"));
            assertFalse(service.removeCDByCode("C001"));
            assertEquals(0, service.searchBooks("").size());
            assertEquals(0, service.searchCD("").size());
        }
    }
    @Test
    void removeByIsbn_ShouldReturnFalse_WhenIsbnNullOrEmpty() {
        BookCDService service = new BookCDService(new ArrayList<>(), new ArrayList<>());

        assertFalse(service.removeByIsbn(null));
        assertFalse(service.removeByIsbn(""));
        assertFalse(service.removeByIsbn("   "));
    }
}