package org.library.Service.Strategy.fines;

import org.library.Service.Strategy.BorrowService;
import org.library.Domain.Loan;
import org.library.Domain.User;

import java.time.LocalDate;

/**
 * Service for fine calculation (Sprint 5: Strategy + mixed media).
 * @author YourName
 * @version 1.0
 */
public class FineCalculator {
    private BorrowService borrowService;

    public FineCalculator(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * Calculate total fine for user (US5.3: accurate across media types).
     * @param user the user
     * @return total NIS
     */
    public int calculateTotalFine(User user) {
        int total = 0;
        for (Loan loan : borrowService.getLoans()) {  // أو stream()
            if (loan.getUser().equals(user) && borrowService.isOverdue(loan)) {
                int overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                total += loan.getMedia().getFineStrategy().calculateFine(overdueDays);
            }
        }
        return total;
    }
}