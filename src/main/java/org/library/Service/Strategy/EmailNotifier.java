package org.library.Service.Strategy;

import org.library.Domain.EmailMessage;
import org.library.Domain.User;



/**


 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad



 * @version 1.1
 */
public class EmailNotifier implements Observer {
    private final EmailServer emailServer;

    /**
     * Constructor.
     * @param emailServer .
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