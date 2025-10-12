package org.example;

/**
 * Report for overdue loans (Sprint 3).
 * @author YourName
 * @version 1.0
 */
public class OverdueReport {
    private final User user;
    private final int overdueBooksCount;

    /**
     * Constructor.
     * @param user the user
     * @param overdueBooksCount the count
     */
    public OverdueReport(User user, int overdueBooksCount) {
        this.user = user;
        this.overdueBooksCount = overdueBooksCount;
    }

    // Getters
    public User getUser() { return user; }
    public int getOverdueBooksCount() { return overdueBooksCount; }
}