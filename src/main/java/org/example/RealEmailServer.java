package org.example;

/**
 * Real email server (production, Sprint 3).
 * @author YourName
 * @version 1.0
 */
public class RealEmailServer implements EmailServer {
    @Override
    public void send(EmailMessage email) {
        System.out.println("Sent to " + email.getRecipientEmail() + ": " + email.getContent());
    }
}