
package org.library;

import org.junit.jupiter.api.Test;
import org.library.Domain.Loan;
import org.library.Domain.Media;
import org.library.Domain.User;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LoanTest {

    @Test
    void shouldCreateLoanWithCorrectValues() {
        // Arrange
        String loanId = "L001";
        Media media = mock(Media.class);  // mock عشان ما نحتاجش كلاس حقيقي
        User user = mock(User.class);
        LocalDate borrowDate = LocalDate.of(2025, 1, 1);
        LocalDate dueDate = LocalDate.of(2025, 1, 15);

        // Act
        Loan loan = new Loan(loanId, media, user, borrowDate, dueDate);

        // Assert
        assertEquals(loanId, loan.getLoanId(), "Loan ID should match");
        assertEquals(media, loan.getMedia(), "Media should match");
        assertEquals(user, loan.getUser(), "User should match");
        assertEquals(borrowDate, loan.getBorrowDate(), "Borrow date should match");
        assertEquals(dueDate, loan.getDueDate(), "Due date should match");
        assertFalse(loan.isReturned(), "New loan should not be returned");
    }

    @Test
    void shouldSetReturnedToTrue() {

        Loan loan = new Loan("L001", mock(Media.class), mock(User.class), LocalDate.now(), LocalDate.now().plusDays(14));


        loan.setReturned(true);


        assertTrue(loan.isReturned(), "Loan should be marked as returned");
    }

    @Test
    void shouldSetReturnedToFalse() {

        Loan loan = new Loan("L001", mock(Media.class), mock(User.class), LocalDate.now(), LocalDate.now().plusDays(14));
        loan.setReturned(true);  // نبدأ بـ true

        loan.setReturned(false);


        assertFalse(loan.isReturned(), "Loan should be unmarked as returned");
    }
}