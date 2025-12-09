package org.library.domain;

import java.util.Objects;

/**
 * Value object for fines (Sprint 4+).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public class Fine {
    private final int amount;
    private boolean paid;

    /**
     * Constructor for new unpaid fine.
     */
    public Fine(int amount) {
        this.amount = amount;
        this.paid = false;
    }

    /**
     * Constructor for loaded fine (from file).
     */
    public Fine(int amount, boolean paid) {
        this.amount = amount;
        this.paid = paid;
    }

    public int getAmount() { return amount; }
    public boolean isPaid() { return paid; }

    /**
     * Mark as paid.
     */
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

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
