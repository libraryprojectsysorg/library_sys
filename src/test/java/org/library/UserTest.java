package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.library.Domain.Fine;
import org.library.Domain.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {

        user = new User("U101", "Ahmad", "ahmad@example.com", "Student");
    }

    @Test
    @DisplayName("Test Constructor and Getters")
    void testConstructor() {
        assertEquals("U101", user.getId());
        assertEquals("Ahmad", user.getName());
        assertEquals("ahmad@example.com", user.getEmail());
        assertEquals("Student", user.getRole());
        assertFalse(user.hasUnpaidFines(), "New user should not have unpaid fines");
        assertTrue(user.getFines().isEmpty(), "Fines list should be empty initially");
    }

    @Test
    @DisplayName("Test addFine updates list and status")
    void testAddFine() {
        // Arrange
        Fine mockFine = mock(Fine.class);
        when(mockFine.isPaid()).thenReturn(false);


        user.addFine(mockFine);


        assertEquals(1, user.getFines().size());
        assertTrue(user.hasUnpaidFines(), "User should be flagged as having unpaid fines");
    }

    @Test
    @DisplayName("Test addFine throws exception for null")
    void testAddFineNull() {
        assertThrows(IllegalArgumentException.class, () -> user.addFine(null));
    }

    @Test
    @DisplayName("Test payFine marks fine as paid and updates status")
    void testPayFine() {

        Fine fine1 = mock(Fine.class);
        when(fine1.isPaid()).thenReturn(false);

        user.addFine(fine1);
        assertTrue(user.hasUnpaidFines());
        boolean result = user.payFine(fine1);


        assertTrue(result, "payFine should return true if fine exists");
        verify(fine1).setPaid(true);
        when(fine1.isPaid()).thenReturn(true);

        user.payFine(fine1);
        assertFalse(user.hasUnpaidFines(), "Status should update to false after paying");
    }

    @Test
    @DisplayName("Test payAllFines pays everything")
    void testPayAllFines() {

        Fine f1 = mock(Fine.class);
        Fine f2 = mock(Fine.class);

        user.addFine(f1);
        user.addFine(f2);


        user.payAllFines();


        verify(f1).setPaid(true);
        verify(f2).setPaid(true);


        when(f1.isPaid()).thenReturn(true);
        when(f2.isPaid()).thenReturn(true);


    }

    @Test
    @DisplayName("Test getTotalUnpaidFine calculation")
    void testGetTotalUnpaidFine() {
        // Arrange
        Fine unpaidFine1 = mock(Fine.class);
        when(unpaidFine1.isPaid()).thenReturn(false);
        when(unpaidFine1.getAmount()).thenReturn(50);

        Fine unpaidFine2 = mock(Fine.class);
        when(unpaidFine2.isPaid()).thenReturn(false);
        when(unpaidFine2.getAmount()).thenReturn(30);

        Fine paidFine = mock(Fine.class);
        when(paidFine.isPaid()).thenReturn(true);
        when(paidFine.getAmount()).thenReturn(100);

        user.addFine(unpaidFine1);
        user.addFine(unpaidFine2);
        user.addFine(paidFine);

        int total = user.getTotalUnpaidFine();

        assertEquals(80, total, "Should sum only unpaid fines (50 + 30)");
    }

    @Test
    @DisplayName("Test setFines replaces list and updates status")
    void testSetFines() {

        List<Fine> newFines = new ArrayList<>();
        Fine f1 = mock(Fine.class);
        when(f1.isPaid()).thenReturn(false);
        newFines.add(f1);


        user.setFines(newFines);

        assertEquals(1, user.getFines().size());
        assertTrue(user.hasUnpaidFines(), "Should flag unpaid fines from the new list");
    }

    @Test
    @DisplayName("Test Equals and HashCode")
    void testEqualsAndHashCode() {
        User user1 = new User("1", "A", "a@a.com");
        User user2 = new User("1", "B", "b@b.com");
        User user3 = new User("2", "A", "a@a.com");

        assertEquals(user1, user2, "Users with same ID should be equal");
        assertNotEquals(user1, user3, "Users with different ID should not be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "HashCodes must match for equal users");
    }

    @Test
    @DisplayName("Test unmodifiable list protection")
    void testGetFinesIsImmutable() {
        List<Fine> fines = user.getFines();
        Fine fine = mock(Fine.class);

        assertThrows(UnsupportedOperationException.class, () -> fines.add(fine));
    }
}