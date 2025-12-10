package org.library.domain;

import org.library.Service.strategy.fines.CDFineStrategy;
import org.library.Service.strategy.fines.FineStrategy;

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 * @see org.library.domain.Media
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
    public int getLoanDays()
    {

        return 7;

    }

    /**
     * * @return .
     */
    @Override
    public FineStrategy getFineStrategy() {
        return new CDFineStrategy();
    }


    @Override
    public int getDailyFineRate(String userRole) {
        if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) return 0;
        if ("LIBRARIAN".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole)) return 10;
        return 20;
    }
}