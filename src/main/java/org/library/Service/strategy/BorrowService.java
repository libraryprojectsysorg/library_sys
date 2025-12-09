package org.library.Service.strategy;

import org.library.domain.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BorrowService {


    private static final Logger LOGGER = Logger.getLogger(BorrowService.class.getName());

    private Clock clock = Clock.systemDefaultZone();
    @SuppressWarnings("unused")
    private final EmailNotifier emailNotifier;
    private final LoanFileHandler loanFileHandler;

    public BorrowService(EmailNotifier emailNotifier, LoanFileHandler loanFileHandler) {
        this.emailNotifier = emailNotifier;
        this.loanFileHandler = loanFileHandler;
    }

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
        if (loanFileHandler.isMediaBorrowed(media.getIsbn())) {
            throw new RuntimeException("This item is already borrowed by another user.");
        }

        LocalDate borrowDate = LocalDate.now(clock);
        LocalDate dueDate = borrowDate.plusDays(media.getLoanDays());
        String loanId = "LOAN_" + System.currentTimeMillis();
        Loan loan = new Loan(loanId, media, user, borrowDate, dueDate);

        loanFileHandler.saveLoan(loan);
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
        loanFileHandler.rewriteAllLoans(activeLoans);
        loan.getMedia().setAvailable(true);
        int fineAmount = calculateFineForLoan(loan);
        if (fineAmount > 0) {
            Fine fine = new Fine(fineAmount);
            FineFileManager.addFineForUser(loan.getUser(), fine);
            LOGGER.log(Level.WARNING, "تم إضافة غرامة للمستخدم: {0} NIS", fineAmount);
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

    public void setClock(Clock mockClock) {
        this.clock = mockClock;
    }

    public Loan borrowCD(CD cd, User user) {
        if (cd == null || user == null) {
            throw new IllegalArgumentException("Invalid CD or user.");
        }
        return borrowMedia(cd, user);
    }
}
