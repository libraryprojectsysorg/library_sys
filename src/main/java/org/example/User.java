package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a library user who can borrow books and receive notifications.
 * @author YourName
 * @version 1.0
 */
public class User {
    private final String id;        // Unique identifier
    private final String name;      // User's full name
    private final String email;     // Email for notifications
    private boolean hasUnpaidFines; // Flag for borrowing restrictions (Sprint 4)
    private List<Fine> fines = new ArrayList<>();  // List of fines (Sprint 4: US4.1, US2.3)

    /**
     * Constructor for User.
     * @param id the unique user ID
     * @param name the user's name
     * @param email the user's email address
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hasUnpaidFines = false;  // Default: no fines
    }

    // Getters (immutable where possible)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean hasUnpaidFines() { return hasUnpaidFines; }

    /**
     * Set fine status (for Sprint 4 integration).
     * @param hasUnpaidFines true if user has unpaid fines
     */
    public void setHasUnpaidFines(boolean hasUnpaidFines) {
        this.hasUnpaidFines = hasUnpaidFines;
    }

    /**
     * Add fine to user (for overdue, Sprint 4+).
     * @param fine the fine to add
     */
    public void addFine(Fine fine) {
        fines.add(fine);
        updateHasUnpaidFines();  // Update flag
    }

    /**
     * Get total unpaid fine (Sprint 5: US5.3 mixed media).
     * @return total NIS unpaid
     */
    public int getTotalUnpaidFine() {
        return (int) fines.stream()
                .filter(f -> !f.isPaid())
                .mapToInt(Fine::getAmount)
                .sum();  // Sum unpaid (covers stream/filter/map branch)
    }

    /**
     * Update flag based on fines (internal).
     */
    private void updateHasUnpaidFines() {
        hasUnpaidFines = fines.stream().anyMatch(f -> !f.isPaid());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}