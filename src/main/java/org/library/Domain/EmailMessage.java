package org.library.Domain;

/**
 * Value object for email notifications (Sprint 3).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public class EmailMessage {
    private final String recipientEmail;
    private final String subject;
    private final String content;

    /**
     * Constructor.
     * @param recipientEmail the user's email
     * @param subject the subject
     * @param content the message body
     */
    public EmailMessage(String recipientEmail, String subject, String content) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.content = content;
    }

    // Getters
    public String getRecipientEmail() { return recipientEmail; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }


}