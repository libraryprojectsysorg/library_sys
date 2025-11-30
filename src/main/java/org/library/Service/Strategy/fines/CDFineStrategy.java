package org.library.Service.Strategy.fines;

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public class CDFineStrategy implements FineStrategy {
    /**

     * @param overdueDays days overdue
     * @return 20 * days NIS
     */
    @Override
    public int calculateFine(int overdueDays) {
        return 20 * overdueDays;
    }
}