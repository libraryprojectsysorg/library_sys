package org.example;

public class CD extends Media {
    public CD(String title, String author, String isbn) {
        super(title, author, isbn);
    }

    @Override
    public int getLoanDays() { return 7; }  // US5.1

    @Override
    public FineStrategy getFineStrategy() { return new CDFineStrategy(); }  // 20 NIS
}