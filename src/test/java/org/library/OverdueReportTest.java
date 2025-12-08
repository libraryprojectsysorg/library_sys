package org.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.library.Domain.OverdueReport;
import org.library.Domain.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OverdueReportTest {

    @Test
    @DisplayName("Test Constructor and Getters")
    void testConstructorAndGetters() {
        // Arrange
        // نستخدم Mock لليوزر لأننا لا نريد الاعتماد على كود User الفعلي هنا
        User mockUser = mock(User.class);
        int count = 5;

        // Act
        OverdueReport report = new OverdueReport(mockUser, count);

        // Assert
        assertEquals(mockUser, report.getUser(), "User object should match the one passed to constructor");
        assertEquals(count, report.getOverdueBooksCount(), "Overdue count should match");
    }

    @Test
    @DisplayName("Test Equals method - Symmetry and Reflexivity")
    void testEquals() {
        // Arrange
        User user1 = mock(User.class);
        User user2 = mock(User.class); // يوزر مختلف

        OverdueReport report1 = new OverdueReport(user1, 3);
        OverdueReport report2 = new OverdueReport(user1, 3); // مطابق لـ report1
        OverdueReport report3 = new OverdueReport(user1, 5); // عدد كتب مختلف
        OverdueReport report4 = new OverdueReport(user2, 3); // يوزر مختلف

        // Assert
        // 1. Reflexive (يساوي نفسه)
        assertEquals(report1, report1);

        // 2. Symmetric (المتطابقين متساويين)
        assertEquals(report1, report2);
        assertEquals(report2, report1);

        // 3. Not Equal scenarios (غير متساويين)
        assertNotEquals(report1, report3, "Should not be equal if count differs");
        assertNotEquals(report1, report4, "Should not be equal if user differs");
        assertNotEquals(report1, null, "Should not be equal to null");
        assertNotEquals(report1, "Some String", "Should not be equal to different class type");
    }

    @Test
    @DisplayName("Test HashCode contract")
    void testHashCode() {
        // Arrange
        User user = mock(User.class);
        OverdueReport report1 = new OverdueReport(user, 10);
        OverdueReport report2 = new OverdueReport(user, 10);

        // Act & Assert
        // إذا كان الاوبجكتين متساويين حسب equals، يجب أن يكون لهما نفس hashCode
        assertEquals(report1, report2);
        assertEquals(report1.hashCode(), report2.hashCode(), "HashCodes must be equal for equal objects");
    }
}