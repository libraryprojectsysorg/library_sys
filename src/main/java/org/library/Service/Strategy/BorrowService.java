/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */




package org.library.Service.Strategy;

import org.library.Domain.Loan;
import org.library.Domain.Media;
import org.library.Domain.User;
import org.library.Domain.Fine;


import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowService {

    private Clock clock = Clock.systemDefaultZone();
    private final EmailNotifier emailNotifier;

    public BorrowService(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    public List<Loan> getLoans() {
        return LoanFileHandler.loadAllLoans();
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

        LoanFileHandler.saveLoan(loan);

        media.setAvailable(false);
        return loan;
    }

    public int returnMedia(Loan loan) {
        List<Loan> activeLoans = getLoans();

        // البحث عن القرض المراد حذفه في القائمة المحملة
        boolean removedFromList = activeLoans.removeIf(l -> l.getMedia().equals(loan.getMedia()));

        if (!removedFromList) {
            throw new IllegalArgumentException("Loan not found in active loans.");
        }

        int fineAmount = calculateFineForLoan(loan);

        LoanFileHandler.rewriteAllLoans(activeLoans); // إعادة كتابة القائمة المحدثة

        loan.getMedia().setAvailable(true);

        if (fineAmount > 0) {
            User user = loan.getUser();
            Fine newFine = new Fine(fineAmount);
            user.addFine(newFine);
            // ملاحظة: يجب تحديث ملف المستخدمين بعد إضافة الغرامة
            return fineAmount;
        }

        return 0;
    }

    public boolean hasActiveLoans(User user) {
        return getLoans().stream().anyMatch(l -> l.getUser().equals(user));
    }

    public void addLoan(Loan loan) {
        LoanFileHandler.saveLoan(loan);
    }

    /** إلغاء تسجيل المستخدم بحذف جميع الإعارات المتعلقة به من الملف. */
    public boolean unregisterUser(String userId) {
        List<Loan> activeLoans = getLoans();
        boolean removed = activeLoans.removeIf(loan -> loan.getUser().getId().equals(userId));

        if (removed) {
            LoanFileHandler.rewriteAllLoans(activeLoans);
        }
        return removed;
    }

    public boolean returnLoan(String loanId) {
        List<Loan> activeLoans = getLoans();

        for (Loan loan : activeLoans) {
            if (loan.getMedia().equals(loanId)) {
                returnMedia(loan);
                return true;
            }
        }
        return false;
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

    /**
     * 🔥 الوظيفة المضافة 2: عدّ القروض المتأخرة لمستخدم معين.
     */
    public int countOverdueLoansForUser(User user) {
        return (int) getLoans().stream()
                .filter(loan -> loan.getUser().equals(user) && isOverdue(loan))
                .count();
    }
    public Clock getClock() {
        return clock;
    }


/**
 * يسمح بتبديل ساعة النظام بساعة وهمية (Mock Clock) للاختبار.
 * @param mockClock الساعة الوهمية الجديدة.
 */
        public void setClock(Clock mockClock) {
            this.clock = mockClock; // 🔥 هذا هو السطر المطلوب
        }
    }

