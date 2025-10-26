package org.library.Domain;

import java.util.Objects;

/**
 * Value object for fines (Sprint 4+).
 * @author YourName
 * @version 1.0
 */
public class Fine {
    private final int amount;  // NIS
    private boolean paid;  // Status

    /**
     * Constructor.
     * @param amount the fine amount
     */
    public Fine(int amount) {
        this.amount = amount;
        this.paid = false;
    }

    public int getAmount() { return amount; }
    public boolean isPaid() { return paid; }

    /**
     * Mark as paid (US2.3 partial/full).
     * @param paid true if paid
     */
    public void setPaid(boolean paid)
    { this.paid = paid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fine fine = (Fine) o;
        return amount == fine.amount && paid == fine.paid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, paid);
    }
}