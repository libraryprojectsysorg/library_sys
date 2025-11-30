package org.library;

import org.junit.jupiter.api.Test;
import org.library.Service.Strategy.fines.BookFineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class BookFineStrategyTest {


    @Test
    void shouldCalculateCorrectFineForOverdueDays() {

        BookFineStrategy strategy = new BookFineStrategy();
        int overdueDays = 5;
        int expectedFine = 50;


        int actualFine = strategy.calculateFine(overdueDays);


        assertEquals(expectedFine, actualFine, "الغرامة يجب أن تكون 50 عند التأخير لمدة 5 أيام");
    }


    @Test
    void shouldReturnZeroFineForZeroDays() {

        BookFineStrategy strategy = new BookFineStrategy();
        int overdueDays = 0;
        int expectedFine = 0;


        int actualFine = strategy.calculateFine(overdueDays);


        assertEquals(expectedFine, actualFine, "الغرامة يجب أن تكون 0 عند عدم وجود تأخير");
    }
}