package org.example;

import java.time.LocalDate;

/**
 * Entity for a media loan (US2.1, Sprint 5: polymorphism for book/CD).
 * @author YourName
 * @version 1.0
 */
public class Loan {
    private final String loanId;
    private final Media media;  // Polymorphism: Book or CD (remove Book field)
    private final User user;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;

    /**
     * Constructor for Loan.
     * @param loanId unique loan ID
     * @param media the borrowed media (Book or CD)
     * @param user the borrowing user
     * @param borrowDate when borrowed
     * @param dueDate due date (+loan days)
     */
    public Loan(String loanId, Media media, User user, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.media = media;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    // Getters (no isOverdue() - moved to service)
    public String getLoanId() { return loanId; }
    public Media getMedia() { return media; }  // For polymorphism (US5.1)
    public User getUser() { return user; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
}