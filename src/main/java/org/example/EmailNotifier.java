package org.example;

/**
 * Email notifier implementing Observer (Sprint 3).
 * @author YourName
 * @version 1.0
 */
public class EmailNotifier implements Observer {  // implements Observer
    private final EmailServer emailServer;

    /**
     * Constructor.
     * @param emailServer the email server
     */
    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override  // IntelliJ يقترحها تلقائياً
    public void notify(User user, String message) {
        String subject = "Overdue Books Reminder";
        EmailMessage email = new EmailMessage(user.getEmail(), subject, message);
        emailServer.send(email);
    }
}