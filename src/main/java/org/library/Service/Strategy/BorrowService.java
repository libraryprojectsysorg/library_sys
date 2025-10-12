package org.library.Service.Strategy;

import org.library.Domain.Loan;
import org.library.Domain.Media;
import org.library.Domain.User;
import org.library.Service.Strategy.EmailNotifier; // (يجب إنشاء هذا الكلاس)

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة منطق الأعمال لإدارة الإعارات ومنطق التأخير (Service Layer).
 * يدعم محاكاة الوقت، قيود الإعارة، تعدد الأشكال، ويدمج نمط الملاحظ (Observer).
 *
 * @author YourName
 * @version 1.2
 */
public class BorrowService {

    private Clock clock = Clock.systemDefaultZone();
    private final List<Loan> loans = new ArrayList<>(); // تم تغيير مستوى الوصول إلى private/final ليتوافق مع DI

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
        // يمكنك إزالة هذا Constructor إذا كنت تستخدم DI في جميع الأماكن
        this.emailNotifier = null; // سيؤدي إلى NullPointerException إذا استخدمت sendReminders()
    }

    /**
     * تعيين كائن ساعة وهمي (Mock Clock) لاستخدامه في الاختبارات.
     * @param clock الساعة الوهمية المراد استخدامها.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return this.clock;
    }

    /**
     * استرجاع قائمة بجميع الإعارات النشطة حالياً. (للاختبارات)
     * @return قائمة الإعارات (List<Loan>).
     */
    public List<Loan> getLoans() {
        return loans;
    }

    // --- Core Business Logic ---

    /**
     * اقتراض وسيط للمستخدم (US2.1, US5.1).
     *
     * @param media الوسيط المراد اقتراضه.
     * @param user المستخدم الذي يقوم بالاقتراض.
     * @return كائن الإعارة الجديد (Loan).
     * @throws RuntimeException إذا كان الوسيط غير متوفر أو تنطبق قيود الإعارة (US4.1).
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
     * إرجاع وسيط مُعار (Media Return). (وظيفة جديدة مضافة)
     *
     * @param loan الإعارة المراد إغلاقها.
     * @return قيمة الغرامة المستحقة على هذه الإعارة (0 إذا لم يكن هناك تأخير).
     * @throws IllegalArgumentException إذا كانت الإعارة غير موجودة.
     */
    public int returnMedia(Loan loan) {
        if (!loans.contains(loan)) {
            throw new IllegalArgumentException("Loan not found in active loans.");
        }

        int fine = calculateFineForLoan(loan);

        loans.remove(loan);
        loan.getMedia().setAvailable(true);

        // هنا يجب تسجيل الغرامة على المستخدم (افتراضياً)
        if (fine > 0) {
            // user.addFine(fine);
        }

        return fine;
    }

    // --- Fine Calculation & Reporting (US5.2, US5.3) ---

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


    // --- Helper Methods ---

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