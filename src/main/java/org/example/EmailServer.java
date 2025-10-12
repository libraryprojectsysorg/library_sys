package org.example;

/**
 * Email server interface (Sprint 3).
 * @author YourName
 * @version 1.0
 */
public interface EmailServer {
    /**
     * Send email.
     * @param email the message
     */
    void send(EmailMessage email);
}