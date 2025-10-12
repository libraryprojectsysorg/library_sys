package org.example;

/**
 * CD fine strategy: 20 NIS/day (US5.2).
 * @author YourName
 * @version 1.0
 */
public class CDFineStrategy implements FineStrategy {
    /**
     * Calculate CD fine.
     * @param overdueDays days overdue
     * @return 20 * days NIS
     */
    @Override
    public int calculateFine(int overdueDays) {
        return 20 * overdueDays;  // US5.2 CD rate
    }
}