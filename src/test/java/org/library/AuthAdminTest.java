package org.library;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.library.Domain.Book;
import org.library.Service.Strategy.BookService;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.fines.FineCalculator;
import org.library.Service.Strategy.ReminderService;
import org.library.Service.Strategy.AuthAdmin;


import java.io.ByteArrayInputStream;



import static org.mockito.Mockito.*;



import java.util.Scanner;

;

class AuthAdminTest {

    AuthAdmin authAdmin;
    BookService bookService;
    BorrowService borrowService;
    ReminderService reminderService;
    FineCalculator fineCalculator;

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        borrowService = mock(BorrowService.class);
        reminderService = mock(ReminderService.class);
        fineCalculator = mock(FineCalculator.class);

        authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookService);

        // تسجيل الدخول كأدمن
        authAdmin.login("s12217663@stu.najah.edu", "ws1234");
    }

    @Test
    void testAddBookInteractive() {
        String simulatedInput = "Test Book\nTest Author\n1234567890\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(bookService.addBook("Test Book", "Test Author", "1234567890")).thenReturn(true);

        authAdmin.addBookInteractive(scanner);

        verify(bookService).addBook("Test Book", "Test Author", "1234567890");
    }

    @Test
    void testDeleteBookInteractive() {
        String simulatedInput = "1234567890\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        Book book = new Book("Test Book", "Test Author", "1234567890");
        when(bookService.searchBooks("1234567890")).thenReturn(java.util.List.of(book));
        when(bookService.removeByIsbn("1234567890")).thenReturn(true);

        authAdmin.deleteBookInteractive(scanner);

        verify(bookService).removeByIsbn("1234567890");
    }

    @Test
    void testViewAllBooks() {
        Book book1 = new Book("Book1", "Author1", "111");
        Book book2 = new Book("Book2", "Author2", "222");
        when(bookService.searchBooks("")).thenReturn(java.util.List.of(book1, book2));

        authAdmin.viewAllBooks();
        verify(bookService).searchBooks("");
    }
}