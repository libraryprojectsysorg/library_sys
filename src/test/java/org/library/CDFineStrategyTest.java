package org.library;

import org.junit.jupiter.api.Test;
import org.library.Service.Strategy.fines.CDFineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class CDFineStrategyTest {

    private final CDFineStrategy strategy = new CDFineStrategy();

    @Test
    void shouldCalculateCorrectFineForPositiveOverdueDays() {
        int overdueDays = 5;
        int expectedFine = 100; // 20 * 5

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة يجب أن تكون 100 عند التأخير لمدة 5 أيام");
    }

    @Test
    void shouldReturnZeroFineForZeroDays() {
        int overdueDays = 0;
        int expectedFine = 0;

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة يجب أن تكون 0 عند عدم وجود تأخير");
    }

    @Test
    void shouldHandleNegativeOverdueDaysGracefully() {
        int overdueDays = -3;
        int expectedFine = -60; // 20 * -3

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة لأي قيمة سالبة من أيام التأخير يجب أن تكون سالبة");
    }

    @Test
    void shouldHandleLargeNumberOfOverdueDays() {
        int overdueDays = 1000;
        int expectedFine = 20000; // 20 * 1000

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة لأي عدد كبير من أيام التأخير يجب أن تحسب بشكل صحيح");
    }
}