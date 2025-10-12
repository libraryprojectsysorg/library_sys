package org.example;

/**
 * Book fine strategy: 10 NIS/day (US5.2).
 * @author YourName
 * @version 1.0
 */
public class BookFineStrategy implements FineStrategy {
    /**
     * Calculate book fine.
     * @param overdueDays days overdue
     * @return 10 * days NIS
     */
    @Override
    public int calculateFine(int overdueDays) {
        return 10 * overdueDays;  // US5.2 book rate
    }
}