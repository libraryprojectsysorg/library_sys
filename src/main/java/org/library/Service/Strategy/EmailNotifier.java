package org.library.Service.Strategy; // (1) تصحيح الحزمة لتناسب نمط الإشعارات

import org.library.Domain.EmailMessage;
import org.library.Domain.User;



/**
 * تطبيق لنمط الملاحظ (Observer) يرسل الإشعارات عبر البريد الإلكتروني.
 *
 * @author YourName
 * @version 1.1
 */
public class EmailNotifier implements Observer {
    private final EmailServer emailServer;

    /**
     * Constructor.
     * @param emailServer the email server
     */
    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    /**

     * @param user .
     * @param message .
     */
    @Override
    public void notify(User user, String message) {
        String subject = "Overdue Books Reminder";


        EmailMessage email = new EmailMessage(user.getEmail(), subject, message);

        emailServer.send(email);
    }
}