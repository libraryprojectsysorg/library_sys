package org.library.domain;

import org.library.Service.strategy.fines.FineStrategy;

/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
public abstract class Media {
    protected final String title, author, isbn;
    protected boolean available = true;

    /**

     * @param title .
     * @param author .
     * @param isbn .
     * @throws IllegalArgumentException .
     */
    public Media(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid media details: title, author, or ISBN cannot be null or empty");
        }
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    /**

     * @return days
     */
    public abstract int getLoanDays();

    /**

     * @return FineStrategy
     */
    public abstract FineStrategy getFineStrategy();

    /**

     * @return title
     */
    public String getTitle() { return title; }

    /**

     * @return author
     */
    public String getAuthor() { return author; }

    /**

     * @return ISBN
     */
    public String getIsbn() { return isbn; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }


    public abstract int getDailyFineRate(String userRole);


}