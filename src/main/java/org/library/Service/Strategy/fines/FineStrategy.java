package org.library.Service.Strategy.fines;

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public interface FineStrategy {
    /**

     * @param overdueDays days overdue
     * @return NIS amount
     */
    int calculateFine(int overdueDays);
}

