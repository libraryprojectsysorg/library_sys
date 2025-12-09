package org.library.Service.Strategy;

import org.library.domain.EmailMessage;

/**


 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public interface EmailServer {
    /**
     * Send email.
     * @param email the message
     */
    void send(EmailMessage email);
}