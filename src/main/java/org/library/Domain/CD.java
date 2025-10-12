package org.library.Domain; // تم توحيد الحزمة إلى الأحرف الصغيرة

import org.library.Service.Strategy.fines.CDFineStrategy; // تصحيح الحزمة
import org.library.Service.Strategy.fines.FineStrategy; // تصحيح الحزمة

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 * @see org.library.Domain.Media
 */
public class CD extends Media {

    /**

     * @param title .
     * @param author .
     * @param isbn .
     */
    public CD(String title, String author, String isbn) {
        super(title, author, isbn);
    }

    /**

     * * @return .
     */
    @Override
    public int getLoanDays() {
        return 7;
    }

    /**

     * * @return .
     */
    @Override
    public FineStrategy getFineStrategy() {
        return new CDFineStrategy();
    }
}