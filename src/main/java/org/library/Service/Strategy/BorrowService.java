/**
 * @author Weam Ahmad
 * @author Seba Abd Aljwwad
 */
package org.library.Service.Strategy;

import org.library.Domain.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowService {

    private Clock clock = Clock.systemDefaultZone();
    private final EmailNotifier emailNotifier;
    private final LoanFileHandler loanFileHandler; // ← Dependency

    // Constructor الجديد
    public BorrowService(EmailNotifier emailNotifier, LoanFileHandler loanFileHandler) {
        this.emailNotifier = emailNotifier;
        this.loanFileHandler = loanFileHandler;
    }

    // Constructor القديم (للـ production)
    public BorrowService(EmailNotifier emailNotifier) {
        this(emailNotifier, new LoanFileHandler());
    }

    public List<Loan> getLoans() {
        return loanFileHandler.loadAllLoans();
    }

    public Loan borrowMedia(Media media, User user) {
        if (!media.isAvailable()) {
            throw new RuntimeException("Book not available");
        }
        if (user.hasUnpaidFines() || hasOverdueLoans(user)) {
            throw new RuntimeException("Cannot borrow: overdue books or unpaid fines");
        }
        LocalDate borrowDate = LocalDate.now(clock);
        LocalDate dueDate = borrowDate.plusDays(media.getLoanDays());
        String loanId = "LOAN_" + System.currentTimeMillis();
        Loan loan = new Loan(loanId, media, user, borrowDate, dueDate);

        loanFileHandler.saveLoan(loan); // ← استخدم الـ dependency
        media.setAvailable(false);
        return loan;
    }

    public int returnMedia(String loanId) {
        List<Loan> activeLoans = getLoans();

        Loan loan = activeLoans.stream()
                .filter(l -> l.getLoanId().equals(loanId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Loan not found in active loans."));

        activeLoans.remove(loan);
        int fineAmount = calculateFineForLoan(loan);
        loanFileHandler.rewriteAllLoans(activeLoans); // ← استخدم الـ dependency
        loan.getMedia().setAvailable(true);

        if (fineAmount > 0) {
            loan.getUser().addFine(new Fine(fineAmount));
        }

        return fineAmount;
    }

    public boolean hasActiveLoans(User user) {
        return getLoans().stream().anyMatch(l -> l.getUser().equals(user));
    }

    public void addLoan(Loan loan) {
        loanFileHandler.saveLoan(loan);
    }

    public boolean unregisterUser(String userId) {
        List<Loan> activeLoans = getLoans();
        boolean removed = activeLoans.removeIf(loan -> loan.getUser().getId().equals(userId));
        if (removed) {
            loanFileHandler.rewriteAllLoans(activeLoans);
        }
        return removed;
    }

    public boolean returnLoan(String loanId) {
        try {
            returnMedia(loanId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public int calculateFineForLoan(Loan loan) {
        if (!isOverdue(loan)) return 0;
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now(clock));
        return loan.getMedia().getFineStrategy().calculateFine((int) overdueDays);
    }

    public boolean isOverdue(Loan loan) {
        return LocalDate.now(clock).isAfter(loan.getDueDate());
    }

    private boolean hasOverdueLoans(User user) {
        return getLoans().stream().anyMatch(l -> l.getUser().equals(user) && isOverdue(l));
    }

    public int calculateTotalFine(User user) {
        return getLoans().stream()
                .filter(loan -> loan.getUser().equals(user))
                .mapToInt(this::calculateFineForLoan)
                .sum();
    }

    public void sendOverdueReminders() {
        if (emailNotifier == null) {
            throw new IllegalStateException("EmailNotifier service is not configured.");
        }

        LocalDate today = LocalDate.now(clock);

        getLoans().stream()
                .filter(loan -> today.isAfter(loan.getDueDate()))
                .collect(Collectors.groupingBy(Loan::getUser))
                .forEach((user, userLoans) -> {
                    int overdueCount = userLoans.size();
                    String message = String.format("تذكير: لديك %d وسائط متأخرة يجب إرجاعها.", overdueCount);
                    // emailNotifier.notify(user, message);
                });
    }

    public List<User> getUsersWithOverdueLoans() {
        return getLoans().stream()
                .filter(this::isOverdue)
                .map(Loan::getUser)
                .distinct()
                .toList();
    }

    public int countOverdueLoansForUser(User user) {
        return (int) getLoans().stream()
                .filter(loan -> loan.getUser().equals(user) && isOverdue(loan))
                .count();
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock mockClock) {
        this.clock = mockClock;
    }
}