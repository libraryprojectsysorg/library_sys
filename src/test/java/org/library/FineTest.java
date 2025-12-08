package org.library;

import org.junit.jupiter.api.Test;
import org.library.Domain.Fine;

import static org.junit.jupiter.api.Assertions.*;

class FineTest {

    @Test
    void constructor_WithOnlyAmount_ShouldCreateUnpaidFine() {
        Fine fine = new Fine(150);

        assertEquals(150, fine.getAmount());
        assertFalse(fine.isPaid());
    }

    @Test
    void constructor_WithAmountAndPaidStatus_ShouldRespectPaidFlag() {
        Fine paidFine = new Fine(200, true);
        Fine unpaidFine = new Fine(200, false);

        assertTrue(paidFine.isPaid());
        assertFalse(unpaidFine.isPaid());
        assertEquals(200, paidFine.getAmount());
        assertEquals(200, unpaidFine.getAmount());
    }

    @Test
    void setPaid_ShouldChangeStatus() {
        Fine fine = new Fine(100);

        assertFalse(fine.isPaid());

        fine.setPaid(true);
        assertTrue(fine.isPaid());

        fine.setPaid(false);
        assertFalse(fine.isPaid());
    }

    @Test
    void equals_ShouldReturnTrue_ForSameValues() {
        Fine fine1 = new Fine(300, true);
        Fine fine2 = new Fine(300, true);

        assertEquals(fine1, fine2);
        assertEquals(fine2, fine1);
        assertEquals(fine1, fine1); // reflexivity
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentAmountOrStatus() {
        Fine base = new Fine(500, false);

        assertNotEquals(base, new Fine(400, false));  // different amount
        assertNotEquals(base, new Fine(500, true));   // different paid status
        assertNotEquals(base, null);
        assertNotEquals(base, "not a Fine");
    }

    @Test
    void hashCode_ShouldBeConsistentWithEquals() {
        Fine fine1 = new Fine(250, true);
        Fine fine2 = new Fine(250, true);

        assertEquals(fine1, fine2);
        assertEquals(fine1.hashCode(), fine2.hashCode());
    }

    @Test
    void hashCode_ShouldDiffer_WhenValuesDiffer() {
        Fine f1 = new Fine(100, false);
        Fine f2 = new Fine(100, true);
        Fine f3 = new Fine(200, false);

        assertNotEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1.hashCode(), f3.hashCode());
    }

    @Test
    void getAmount_ShouldReturnCorrectValue() {
        Fine fine = new Fine(999);
        assertEquals(999, fine.getAmount());
    }

    @Test
    void shouldBeImmutableExceptForPaidStatus() {
        Fine fine = new Fine(123);

        // amount is final → can't be changed
        assertEquals(fine.getAmount(), 123);

        fine.setPaid(true);
        assertTrue(fine.isPaid());
    }
}