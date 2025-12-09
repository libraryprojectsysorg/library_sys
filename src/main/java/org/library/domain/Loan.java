package org.library.domain;

import java.time.LocalDate;

public class Loan {

    private final String loanId;
    private final Media media;
    private final User user;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private boolean returned;

    public Loan(String loanId, Media media, User user, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.media = media;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = false;
    }

    public String getLoanId() { return loanId; }
    public Media getMedia() { return media; }
    public User getUser() { return user; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}
