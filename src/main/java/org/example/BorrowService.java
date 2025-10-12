package org.example;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for borrowing and overdue logic (Sprint 2-5).
 * Supports injectable Clock for time mocking, restrictions (Sprint 4), and media polymorphism (Sprint 5).
 *
 * @author YourName
 * @version 1.0
 */
public class BorrowService {
    private Clock clock = Clock.systemDefaultZone();  // Default clock
    final List<Loan> loans = new ArrayList<>();  // In-memory repo (package-private for access)

    /**
     * Set clock for testing time manipulation.
     * @param clock the mock clock to use
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return this.clock;  // إرجاع الساعة الحالية
    }



    public List<Loan> getLoans() {
        return loans;  // إرجاع القائمة (للاختبارات)
    }

    /**
     * Borrow media for a user (US2.1, US5.1: polymorphism for book/CD).
     * @param media the media to borrow (Book or CD)
     * @param user the user borrowing
     * @return the created Loan
     * @throws RuntimeException if unavailable or restrictions apply (US4.1)
     */
    public Loan borrowMedia(Media media, User user) {  // General for Media (Sprint 5 polymorphism)
        if (!media.isAvailable()) {
            throw new RuntimeException("Book not available");
        }
        if (user.hasUnpaidFines() || hasOverdueLoans(user)) {  // US4.1 restrictions
            throw new RuntimeException("Cannot borrow: overdue books or unpaid fines");  // إصلاح الرسالة لتطابق الاختبار
        }
        LocalDate borrowDate = LocalDate.now(clock);  // Use clock
        LocalDate dueDate = borrowDate.plusDays(media.getLoanDays());  // Polymorphism: 28 book, 7 CD (US5.1)
        String loanId = "LOAN_" + System.currentTimeMillis();
        Loan loan = new Loan(loanId, media, user, borrowDate, dueDate);  // Loan with Media
        loans.add(loan);
        media.setAvailable(false);  // Mark as borrowed
        return loan;
    }

    /**
     * Check if a loan is overdue (US2.2).
     * @param loan the loan to check
     * @return true if now (from clock) > dueDate
     */
    public boolean isOverdue(Loan loan) {
        return LocalDate.now(clock).isAfter(loan.getDueDate());  // Use clock for mocking
    }

    // For Sprint 3: Get users with overdues
    public List<User> getUsersWithOverdueLoans() {
        return loans.stream()
                .filter(loan -> isOverdue(loan))  // Use service method
                .map(Loan::getUser)
                .distinct()
                .toList();
    }

    public int countOverdueLoansForUser(User user) {
        return (int) loans.stream()
                .filter(loan -> loan.getUser().equals(user) && isOverdue(loan))
                .count();
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    /**
     * Check if user has overdue loans (US4.1 private helper).
     * @param user the user
     * @return true if has overdue
     */
    private boolean hasOverdueLoans(User user) {
        return loans.stream().anyMatch(l -> l.getUser().equals(user) && isOverdue(l));  // Stream branch for coverage
    }

    /**
     * Calculate fine for a loan (US5.2: Strategy integration).
     * @param loan the loan
     * @return NIS amount
     */
    public int calculateFineForLoan(Loan loan) {
        if (!isOverdue(loan)) return 0;
        int overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now(clock));
        return loan.getMedia().getFineStrategy().calculateFine(overdueDays);  // Strategy (US5.2)
    }

    /**
     * Unregister user by removing all associated loans (US3.1).
     * @param userId the user ID to unregister
     * @return true if user had loans and they were removed
     */
    public boolean unregisterUser(String userId) {
        // تنفيذ بسيط: ابحث عن القروض المرتبطة بالمستخدم وحذفها
        boolean removed = loans.removeIf(loan -> loan.getUser().getId().equals(userId));
        return removed;  // true إذا تم حذف أي قرض
    }

}