package org.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Domain.Book;
import org.library.Domain.CD;
import org.library.Domain.Loan;
import org.library.Domain.User;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.fines.FineCalculator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineCalculatorTest {

    @Mock
    private BorrowService borrowService;

    @InjectMocks
    private FineCalculator fineCalculator;

    @Test
    void shouldCalculateFine_ForOverdueBook_UserRole() {

        User user = new User("U01", "John", "john@test.com", "USER");


        Book book = mock(Book.class);
        when(book.getDailyFineRate("USER")).thenReturn(10);


        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(user);
        when(loan.getMedia()).thenReturn(book);


        when(loan.getDueDate()).thenReturn(LocalDate.now().minusDays(5));


        when(borrowService.getLoans()).thenReturn(List.of(loan));
        when(borrowService.isOverdue(loan)).thenReturn(true);


        int totalFine = fineCalculator.calculateTotalFine(user);


        assertEquals(50, totalFine, "يجب حساب الغرامة بـ 50 للكتاب المتأخر 5 أيام");
    }

    @Test
    void shouldCalculateFine_ForOverdueCD_AdminRole() {

        User admin = new User("A01", "Admin", "admin@test.com", "ADMIN");

        CD cd = mock(CD.class);
        when(cd.getDailyFineRate("ADMIN")).thenReturn(5);

        Loan loan = mock(Loan.class);
        when(loan.getUser()).thenReturn(admin);
        when(loan.getMedia()).thenReturn(cd);
        when(loan.getDueDate()).thenReturn(LocalDate.now().minusDays(3));

        when(borrowService.getLoans()).thenReturn(List.of(loan));
        when(borrowService.isOverdue(loan)).thenReturn(true);


        int totalFine = fineCalculator.calculateTotalFine(admin);


        assertEquals(15, totalFine);
    }

    @Test
    void shouldReturnZero_WhenNoLoansAreOverdue() {
        // Arrange
        User user = new User("U01", "Happy User", "happy@test.com", "USER");


        when(borrowService.getLoans()).thenReturn(Collections.emptyList());


        int totalFine = fineCalculator.calculateTotalFine(user);


        assertEquals(0, totalFine, "الغرامة يجب أن تكون 0 إذا لم تكن هناك قروض");
    }

    @Test
    void shouldReturnZero_WhenLoanIsNotOverdue() {

        User user = new User("U01", "OnTime User", "ontime@test.com", "USER");
        Loan loan = mock(Loan.class);

        when(loan.getUser()).thenReturn(user);
        when(borrowService.getLoans()).thenReturn(List.of(loan));


        when(borrowService.isOverdue(loan)).thenReturn(false);


        int totalFine = fineCalculator.calculateTotalFine(user);


        assertEquals(0, totalFine);
    }

    @Test
    void shouldIgnoreLoansForOtherUsers() {

        User user1 = new User("U1", "Me", "me@test.com", "USER");
        User user2 = new User("U2", "Other", "other@test.com", "USER");

        Loan loanForOtherUser = mock(Loan.class);
        when(loanForOtherUser.getUser()).thenReturn(user2);

        when(borrowService.getLoans()).thenReturn(List.of(loanForOtherUser));


        int totalFine = fineCalculator.calculateTotalFine(user1);


        assertEquals(0, totalFine, "يجب تجاهل قروض المستخدمين الآخرين");
    }
}