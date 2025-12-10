package org.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.library.domain.EmailMessage;

import static org.junit.jupiter.api.Assertions.*;

class EmailMessageTest {

    @Test
    @DisplayName("Should create email message correctly and return valid data")
    void testConstructorAndGetters() {

        String recipient = "student@library.org";
        String subject = "Book Due Reminder";
        String content = "Dear Student, please return the book.";


        EmailMessage emailMessage = new EmailMessage(recipient, subject, content);


        assertNotNull(emailMessage, "Object should not be null");
        assertEquals(recipient, emailMessage.getRecipientEmail(), "Recipient email should match");
        assertEquals(subject, emailMessage.getSubject(), "Subject should match");
        assertEquals(content, emailMessage.getContent(), "Content should match");
    }

    @Test
    @DisplayName("Should accept null values if no validation exists")
    void testConstructorWithNulls() {



        EmailMessage message = new EmailMessage(null, null, null);


        assertNull(message.getRecipientEmail());
        assertNull(message.getSubject());
        assertNull(message.getContent());
    }

    @Test
    @DisplayName("Should accept empty strings")
    void testConstructorWithEmptyStrings() {

        EmailMessage message = new EmailMessage("", "", "");


        assertEquals("", message.getRecipientEmail());
        assertEquals("", message.getSubject());
        assertEquals("", message.getContent());
    }
}