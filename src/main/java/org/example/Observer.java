package org.example;

/**
 * Observer interface for notifications (Sprint 3).
 * Allows multiple notification channels (e.g., email, SMS, push).
 *
 * @author YourName
 * @version 1.0
 */
public interface Observer {
    /**
     * Notify the observer with a message for overdue books.
     *
     * @param user the recipient user
     * @param message the notification message (e.g., "You have n overdue book(s).")
     */
    void notify(User user, String message);
}