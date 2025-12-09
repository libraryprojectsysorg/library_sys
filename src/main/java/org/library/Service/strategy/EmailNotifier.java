package org.library.Service.strategy;

import org.library.domain.EmailMessage;
import org.library.domain.User;



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