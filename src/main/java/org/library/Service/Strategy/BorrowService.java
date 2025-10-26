package org.library.Service.Strategy;

import org.library.Domain.Loan;
import org.library.Domain.Media;
import org.library.Domain.User;
import org.library.Domain.Fine;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**

 * يدعم محاكاة الوقت، قيود الإعارة، تعدد الأشكال، ويدمج نمط الملاحظ (Observer).
 *
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.2
 */
public class BorrowService {

    private Clock clock = Clock.systemDefaultZone();
    private final List<Loan> loans = new ArrayList<>();

    /** كائن الملاحظ (Observer) لإرسال الإشعارات عند التأخير. */
    private final EmailNotifier emailNotifier;

    /**
     * منشئ (Constructor) يقوم بحقن التبعيات (Dependency Injection).
     *
     * @param emailNotifier كائن خدمة الإشعارات (Observer).
     */
    public BorrowService(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    // --- Clock and Access Methods ---

    /**
     * منشئ افتراضي يُستخدم في حال عدم وجود نظام حقن تبعيات. (يفضل حذفه في مشروع DI كامل)
     */
    public BorrowService() {

        this.emailNotifier = null;
    }

    /**
     * (Mock Clock)
     * @param clock .
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return this.clock;
    }

    /**
     * @return قائمة الإعارات (List<Loan>).
     */
    public List<Loan> getLoans() {
        return loans;
    }



    /**

     *
     * @param media .
     * @param user .
     * @return .
     * @throws RuntimeException .
     */
    public Loan borrowMedia(Media media, User user) {
        if (!media.isAvailable()) {
            throw new RuntimeException("Book not available");
        }
        // US4.1 restrictions: Check for unpaid fines OR overdue loans
        if (user.hasUnpaidFines() || hasOverdueLoans(user)) {
            throw new RuntimeException("Cannot borrow: overdue books or unpaid fines");
        }
        LocalDate borrowDate = LocalDate.now(clock);
        LocalDate dueDate = borrowDate.plusDays(media.getLoanDays()); // Polymorphism
        String loanId = "LOAN_" + System.currentTimeMillis();
        Loan loan = new Loan(loanId, media, user, borrowDate, dueDate);
        loans.add(loan);
        media.setAvailable(false);
        return loan;
    }



    /**
     * (Media Return).
     *
     * @param loan .
     * @return .
     * @throws IllegalArgumentException .
     */
    public int returnMedia(Loan loan) {
        if (!loans.contains(loan)) {
            throw new IllegalArgumentException("Loan not found in active loans.");
        }

        int fineAmount = calculateFineForLoan(loan);

        loans.remove(loan);
        loan.getMedia().setAvailable(true);


        if (fineAmount > 0) {

            User user = loan.getUser();
            Fine newFine = new Fine(fineAmount);
            user.addFine(newFine);
            return fineAmount;
        }


        return 0;
    }



    /**
     * حساب الغرامة المستحقة على إعارة معينة (US5.2: Strategy integration).
     *
     * @param loan الإعارة المراد حساب غرامتها.
     * @return قيمة الغرامة بالـ NIS.
     */
    public int calculateFineForLoan(Loan loan) {
        if (!isOverdue(loan)) return 0;
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now(clock));
        return loan.getMedia().getFineStrategy().calculateFine((int) overdueDays);
    }

    /**
     * حساب إجمالي الغرامات المستحقة على مستخدم معين (US5.3). (وظيفة جديدة مضافة)
     *
     * @param user المستخدم المراد حساب الغرامات له.
     * @return إجمالي الغرامة بالـ NIS.
     */
    public int calculateTotalFine(User user) {
        return loans.stream()
                .filter(loan -> loan.getUser().equals(user))
                .mapToInt(this::calculateFineForLoan)
                .sum();
    }

    // --- Reminder Service (US3.2) ---

    /**
     * إرسال إشعار تذكير بالتأخير للمستخدمين (Observer Pattern US3.2). (وظيفة جديدة مضافة)
     */
    public void sendOverdueReminders() {
        if (emailNotifier == null) {
            throw new IllegalStateException("EmailNotifier service is not configured.");
        }

        LocalDate today = LocalDate.now(clock);

        loans.stream()
                .filter(loan -> today.isAfter(loan.getDueDate()))
                .collect(Collectors.groupingBy(Loan::getUser)) // تجميع حسب المستخدم
                .forEach((user, userLoans) -> {
                    int overdueCount = userLoans.size();
                    String message = String.format("تذكير: لديك %d وسائط متأخرة يجب إرجاعها.", overdueCount);
                    emailNotifier.notify(user, message); // استدعاء الملاحظ (Observer)
                });
    }




    /**
     * Check if a loan is overdue (US2.2).
     * @param loan the loan to check
     * @return true if now (from clock) > dueDate
     */
    public boolean isOverdue(Loan loan) {
        return LocalDate.now(clock).isAfter(loan.getDueDate());
    }

    /**
     * Check if user has overdue loans (US4.1 private helper).
     * @param user the user
     * @return true if has overdue
     */
    private boolean hasOverdueLoans(User user) {
        return loans.stream().anyMatch(l -> l.getUser().equals(user) && isOverdue(l));
    }

    /**
     * Check if user has *any* active loans (used by AuthAdmin for US4.2 unregister restriction).
     * @param user the user
     * @return true if has any active loan (overdue or not)
     */
    public boolean hasActiveLoans(User user) {
        return loans.stream().anyMatch(l -> l.getUser().equals(user));
    }

    /**
     * Get users with overdues (for reporting/reminders).
     */
    public List<User> getUsersWithOverdueLoans() {
        return loans.stream()
                .filter(this::isOverdue)
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
     * Unregister user by removing all associated loans (US3.1).
     * @param userId the user ID to unregister
     * @return true if user had loans and they were removed
     */
    public boolean unregisterUser(String userId) {
        boolean removed = loans.removeIf(loan -> loan.getUser().getId().equals(userId));
        return removed;
    }
}