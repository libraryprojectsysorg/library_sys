package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.Book;
import org.library.Domain.CD;
import org.library.Domain.User;
import org.library.Domain.Loan;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class AuthAdminTest {

    AuthAdmin authAdmin;
    BookCDService bookCDService;
    BorrowService borrowService;
    ReminderService reminderService;
    FineCalculator fineCalculator;

    User mockAdmin;

    @BeforeEach
    void setUp() {
        bookCDService = mock(BookCDService.class);
        borrowService = mock(BorrowService.class);
        reminderService = mock(ReminderService.class);
        fineCalculator = mock(FineCalculator.class);

        authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookCDService);

        mockAdmin = mock(User.class);
        when(mockAdmin.getId()).thenReturn("U1");
        when(mockAdmin.getName()).thenReturn("AdminA");

        authAdmin.login("s12217663@stu.najah.edu", "hcqn vhpj tlxr xuqk");
    }

    @Test
    void testAddBookInteractive() {
        String input = "Test Book\nTest Author\n123\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        when(bookCDService.addBook("Test Book", "Test Author", "123")).thenReturn(true);
        authAdmin.addBookInteractive(scanner);
        verify(bookCDService).addBook("Test Book", "Test Author", "123");
    }

    @Test
    void testDeleteBookInteractive() {
        String input = "123\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        when(bookCDService.removeByIsbn("123")).thenReturn(true);
        authAdmin.deleteBookInteractive(scanner);
        verify(bookCDService).removeByIsbn("123");
    }

    @Test
    void testViewAllBooks() {
        when(bookCDService.searchBooks("")).thenReturn(List.of(new Book("A", "AA", "1")));
        authAdmin.viewAllBooks();
        verify(bookCDService).searchBooks("");
    }

    @Test
    void testAddCDInteractive() {
        String input = "C1\nMy CD\nArtist\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.addCDInteractive(scanner);
    }

    @Test
    void testDeleteCDInteractive() {
        String input = "C1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.deleteCDInteractive(scanner);
    }

    @Test
    void testViewAllCDs() {
        authAdmin.viewAllCDs();
    }

    @Test
    void testBorrowBookInteractive() {
        Book book = new Book("soft", "haya", "159");
        when(bookCDService.searchBooks("soft")).thenReturn(List.of(book));
        String input = "soft\nU1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.borrowBookInteractive(scanner);

    }

    @Test
    void testBorrowCDInteractive() {
        CD cd = new CD("CD A", "Author Z", "CD1");
        when(bookCDService.searchCD("CD A")).thenReturn(List.of(cd));
        String input = "CD A\nU1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.borrowCDInteractive(scanner);

    }

    @Test
    void testReturnBookInteractive() {
        Book book = new Book("BookA", "AA", "1");
        Loan loan = new Loan("L1", book, mockAdmin, LocalDate.now(), LocalDate.now().plusDays(7));
        when(borrowService.getLoans()).thenReturn(List.of(loan));
        when(borrowService.returnMedia("L1")).thenReturn(0);
        String input = "BookA\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.returnBookInteractive(scanner);
        verify(borrowService).returnMedia("L1");
    }

    @Test
    void testReturnCDInteractive() {
        CD cd = new CD("CD A", "AA", "CD1");
        Loan loan = new Loan("L999", cd, mockAdmin, LocalDate.now(), LocalDate.now().plusDays(7));
        when(borrowService.getLoans()).thenReturn(List.of(loan));
        when(borrowService.returnMedia("L999")).thenReturn(5);
        String input = "CD1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.returnCDInteractive(scanner);
        verify(borrowService).returnMedia("L999");
    }

    @Test
    void testFineSummary() {
        when(fineCalculator.calculateTotalFine(mockAdmin)).thenReturn(20);
        String input = "U1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        authAdmin.fineSummaryInteractive(scanner);

    }
}
