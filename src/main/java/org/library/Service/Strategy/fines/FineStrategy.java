package org.library.Service.Strategy.fines;

/**
 * Strategy for fine calculation (Sprint 5).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public interface FineStrategy {
    /**
     * Calculate fine.
     * @param overdueDays days overdue
     * @return NIS amount
     */
    int calculateFine(int overdueDays);
}

