package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User (Domain Layer, Sprints 2-3).
 * Covers constructor, getters, setter, equals/hashCode, edges.
 *
 * @author YourName
 * @version 1.0
 */
public class UserTest {
    @Test
    void testUserCreationAndGetters() {
        User user = new User("U001", "John Doe", "john@example.com");
        assertEquals("U001", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertFalse(user.hasUnpaidFines());  // Default false (covers init branch)
    }

    @Test
    void testSetHasUnpaidFinesTrue() {
        User user = new User("U002", "Jane Doe", "jane@example.com");
        user.setHasUnpaidFines(true);  // True branch
        assertTrue(user.hasUnpaidFines());
    }

    @Test
    void testSetHasUnpaidFinesFalse() {
        User user = new User("U003", "Bob Doe", "bob@example.com");
        user.setHasUnpaidFines(false);  // False branch
        assertFalse(user.hasUnpaidFines());
    }

    @Test
    void testEqualsSameId() {
        User user1 = new User("U004", "Same User", "same1@example.com");
        User user2 = new User("U004", "Different Name", "same2@example.com");
        assertEquals(user1, user2);  // Equals branch: same ID
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testEqualsDifferentId() {
        User user1 = new User("U005", "User1", "u1@example.com");
        User user2 = new User("U006", "User2", "u2@example.com");
        assertNotEquals(user1, user2);  // Not equals branch
    }

    @Test
    void testEqualsNullOrWrongClass() {
        User user = new User("U007", "Test", "test@example.com");
        assertNotEquals(user, null);  // Null branch
        assertNotEquals(user, "string");  // Wrong class branch
        assertEquals(user, user);  // Self equals
    }

    @Test
    void testNullParamsInConstructor() {
        User user = new User(null, null, null);  // Edge: nulls (covers no-crash)
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
    }
}