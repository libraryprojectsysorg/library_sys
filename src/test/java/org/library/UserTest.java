package org.library;

import org.junit.jupiter.api.Test;
import org.library.Domain.User;
import org.library.Domain.Fine; // (1) يجب استيراد كائن الغرامة

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User (Domain Layer, Sprints 2-4).
 * Covers constructor, getters, equals/hashCode, and fine logic (Sprint 4).
 *
 * @author YourName
 * @version 1.1
 */
public class UserTest {

    // Test 1: إنشاء المستخدم
    @Test
    void testUserCreationAndGetters() {
        User user = new User("U001", "John Doe", "john@example.com");
        assertEquals("U001", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertFalse(user.hasUnpaidFines()); // Default false
    }

    // --- تم إزالة testSetHasUnpaidFines ---

    // (2) اختبار جديد: التحقق من أن إضافة غرامة غير مدفوعة تغير حالة القيود
    @Test
    void testAddingUnpaidFineUpdatesRestrictionStatus() {
        User user = new User("U002", "Jane Doe", "jane@example.com");
        assertFalse(user.hasUnpaidFines());

        // Act: إضافة غرامة غير مدفوعة (الغرامات تبدأ كـ unpaid=false)
        Fine fine = new Fine(50);
        user.addFine(fine);

        // Assert: يجب أن تكون الحالة الآن True
        assertTrue(user.hasUnpaidFines());
    }

    // (3) اختبار جديد: التحقق من أن دفع الغرامة يزيل القيود
    @Test
    void testPayingFineRemovesRestrictionStatus() {
        User user = new User("U003", "Bob Doe", "bob@example.com");
        Fine fine1 = new Fine(50);
        Fine fine2 = new Fine(100);

        user.addFine(fine1);
        user.addFine(fine2);
        assertTrue(user.hasUnpaidFines()); // لديه غرامتان غير مدفوعتين

        // Act 1: دفع الغرامة الأولى
        user.payFine(fine1); // يجب أن تكون هذه الدالة موجودة في كلاس User
        assertTrue(user.hasUnpaidFines()); // ما زال لديه fine2 غير مدفوعة

        // Act 2: دفع الغرامة الثانية
        user.payFine(fine2);

        // Assert: لم يعد لديه أي غرامات غير مدفوعة
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
        // إذا كان Constructor يقبل nulls:
        User user = new User(null, null, null);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        // ملاحظة: يُفضل إضافة التحقق من Null في Constructor لمنطق قوي.
    }
}