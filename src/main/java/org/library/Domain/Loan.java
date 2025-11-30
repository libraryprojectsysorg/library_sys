package org.library.Domain;

import java.time.LocalDate;

/**



 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 */
public class Loan {


    private final String loanId;


    private final Media media;


    private final User user;


    private final LocalDate borrowDate;


    private final LocalDate dueDate;

    private boolean returned;
    /**

     * * @param loanId .
     * @param media .
     * @param user .
     * @param borrowDate .
     * @param dueDate .
     */



    public Loan(String loanId, Media media, User user, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.media = media;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = false;
    }

    /**

     * * @return  (String).
     */
    public String getLoanId() { return loanId; }

    /**

     * * @return  (Media).
     */
    public Media getMedia() { return media; }

    /**

     * * @return (User).
     */
    public User getUser() { return user; }

    /**

     * * @return (LocalDate).
     */
    public LocalDate getBorrowDate() { return borrowDate; }

    /**

     * * @return (LocalDate).
     */
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() {
        return returned;
    }


}