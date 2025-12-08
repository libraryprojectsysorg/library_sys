package org.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.library.Domain.EmailMessage;

import static org.junit.jupiter.api.Assertions.*;

class EmailMessageTest {

    @Test
    @DisplayName("Should create email message correctly and return valid data")
    void testConstructorAndGetters() {
        // Arrange (تجهيز البيانات)
        String recipient = "student@library.org";
        String subject = "Book Due Reminder";
        String content = "Dear Student, please return the book.";

        // Act (تنفيذ العملية)
        EmailMessage emailMessage = new EmailMessage(recipient, subject, content);

        // Assert (التحقق من النتائج)
        assertNotNull(emailMessage, "Object should not be null");
        assertEquals(recipient, emailMessage.getRecipientEmail(), "Recipient email should match");
        assertEquals(subject, emailMessage.getSubject(), "Subject should match");
        assertEquals(content, emailMessage.getContent(), "Content should match");
    }

    @Test
    @DisplayName("Should accept null values if no validation exists")
    void testConstructorWithNulls() {
        /**
         * ملاحظة: الكلاس الحالي لا يمنع القيم الفارغة (Null).
         * هذا التيست يوثق هذا السلوك (أنه يقبل Null ولا يرمي Exception).
         */

        // Act
        EmailMessage message = new EmailMessage(null, null, null);

        // Assert
        assertNull(message.getRecipientEmail());
        assertNull(message.getSubject());
        assertNull(message.getContent());
    }

    @Test
    @DisplayName("Should accept empty strings")
    void testConstructorWithEmptyStrings() {
        // Act
        EmailMessage message = new EmailMessage("", "", "");

        // Assert
        assertEquals("", message.getRecipientEmail());
        assertEquals("", message.getSubject());
        assertEquals("", message.getContent());
    }
}