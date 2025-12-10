package org.library;

import org.junit.jupiter.api.Test;
import org.library.Service.strategy.fines.BookFineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class BookFineStrategyTest {

    private final BookFineStrategy strategy = new BookFineStrategy();

    @Test
    void shouldCalculateCorrectFineForPositiveOverdueDays() {
        int overdueDays = 5;
        int expectedFine = 50;

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة يجب أن تكون 50 عند التأخير لمدة 5 أيام");
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
        int expectedFine = -30;

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة لأي قيمة سالبة من أيام التأخير يجب أن تكون سالب");
    }

    @Test
    void shouldHandleLargeNumberOfOverdueDays() {
        int overdueDays = 1000;
        int expectedFine = 10000;

        int actualFine = strategy.calculateFine(overdueDays);

        assertEquals(expectedFine, actualFine, "الغرامة لأي عدد كبير من أيام التأخير يجب أن تحسب بشكل صحيح");
    }
}
