package org.library.Service.Strategy;

import org.library.Domain.OverdueReport;
import org.library.Domain.User;

import java.util.List;

/**
 * Service for reminders using Observer (Sprint 3).
 * @author YourName
 * @version 1.0
 */
public class ReminderService {
    private final List<Observer> notifiers;
    private final BorrowService borrowService;

    /**
     * Constructor.
     * @param notifiers list of observers (e.g., EmailNotifier)
     * @param borrowService for overdue data
     */
    public ReminderService(List<Observer> notifiers, BorrowService borrowService) {
        this.notifiers = notifiers;
        this.borrowService = borrowService;
    }

    /**
     * Send reminders (US3.1).
     */
    public void sendReminders() {
        List<User> usersWithOverdues = borrowService.getUsersWithOverdueLoans();
        for (User user : usersWithOverdues) {
            int count = borrowService.countOverdueLoansForUser(user);
            OverdueReport report = new OverdueReport(user, count);
            String message = "You have " + report.getOverdueBooksCount() + " overdue book(s).";
            for (Observer notifier : notifiers) {
                notifier.notify(user, message);
            }
        }
    }
}