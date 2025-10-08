package org.example;


public class Book {
    private String title;
    private String author;
    private String isbn;
    private boolean available = true;


    public Book(String title, String author, String isbn) {
        if (title == null || author == null || isbn == null || title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("Invalid book details: title, author, or ISBN cannot be null or empty");  // Branch for validation
        }
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }


    public String getTitle() { return title; }


    public void setTitle(String title) { this.title = title; }


    public String getAuthor() { return author; }


    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }


    public void setIsbn(String isbn) { this.isbn = isbn; }


    public boolean isAvailable() { return available; }


    public void setAvailable(boolean available) { this.available = available; }
}