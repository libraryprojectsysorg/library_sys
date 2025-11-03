package org.library.Domain;

import java.util.Objects;

/**
 * Value object for overdue loans report (Sprint 3).
 *
 * @author YourName
 * @version 1.1
 */
public class OverdueReport {
    private final User user;
    private final int overdueBooksCount;

    /**
     * @author Weam Ahmad
     * @author  Seba Abd Aljwwad
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverdueReport that = (OverdueReport) o;
        // يتم المقارنة بناءً على الحقلين user و overdueBooksCount
        return overdueBooksCount == that.overdueBooksCount &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, overdueBooksCount);
    }
}