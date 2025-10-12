package org.library.Service.Strategy.fines; // تغيير الحزمة للتوافق مع هيكل المشروع المعماري

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public class BookFineStrategy implements FineStrategy {

    /**

     * @param overdueDays .
     * @return .
     */
    @Override
    public int calculateFine(int overdueDays) {
        return 10 * overdueDays;
    }
}