package org.library.Service.strategy;

import org.library.domain.User;

/**


 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public interface Observer {
    /**
     * Notify the observer with a message for overdue books.
     *
     * @param user the recipient user
     * @param message the notification message (e.g., "You have n overdue book(s).")
     */
    void notify(User user, String message);
}