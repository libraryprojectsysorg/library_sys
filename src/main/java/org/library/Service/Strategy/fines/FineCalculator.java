

package org.library.Service.Strategy.fines;


import org.library.Domain.Book;
import org.library.Domain.CD;
import org.library.Domain.Loan;
import org.library.Domain.User;
import org.library.Service.Strategy.BorrowService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineCalculator {
    private final BorrowService borrowService;

    public FineCalculator(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * @param user the user
     * @return total NIS
     */
    public int calculateTotalFine(User user) {
        int total = 0;
        LocalDate today = LocalDate.now();
        String userRole = user.getRole();

        for (Loan loan : borrowService.getLoans()) {
            if (loan.getUser().equals(user) && borrowService.isOverdue(loan)) {

                long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), today);
                int dailyFineRate = 0;


                if (loan.getMedia() instanceof Book book) {
                    dailyFineRate = book.getDailyFineRate(userRole);
                } else if (loan.getMedia() instanceof CD cd) {
                    dailyFineRate = cd.getDailyFineRate(userRole);
                }

                if (overdueDays > 0 && dailyFineRate > 0) {
                    total += (int) (overdueDays * dailyFineRate);
                }
            }
        }

        return total;
    }
}