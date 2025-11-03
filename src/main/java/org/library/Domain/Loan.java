package org.library.Domain;

import java.time.LocalDate;

/**
 * يمثل كيان إعارة وسيط (Loan Entity).
 * يخزن حالة الإعارة (الوسيط، المستخدم، التواريخ).
 * يتميز بدعم تعدد الأشكال للتعامل مع الكتب والأقراص المدمجة (Sprint 5).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 */
public class Loan {

    /** المعرف الفريد للإعارة (Loan ID). يستخدم للتعقب والرجوع. */
    private final String loanId;

    /** الوسيط المُعار (Media). يمكن أن يكون Book أو CD، يستخدم تعدد الأشكال. */
    private final Media media;

    /** المستخدم الذي قام بالإعارة. */
    private final User user;

    /** تاريخ الاقتراض الفعلي للوسيط. */
    private final LocalDate borrowDate;

    /** تاريخ استحقاق الإرجاع، يتم تحديده بناءً على نوع الوسيط. */
    private final LocalDate dueDate;

    /**
     * منشئ (Constructor) جديد لكائن الإعارة.
     * * @param loanId المعرف الفريد للإعارة.
     * @param media الوسيط المُعار (كائن من نوع Media).
     * @param user المستخدم الذي قام بالإعارة.
     * @param borrowDate تاريخ الإعارة الفعلي.
     * @param dueDate تاريخ استحقاق الإرجاع.
     */
    public Loan(String loanId, Media media, User user, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.media = media;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    /**
     * استرجاع المعرف الفريد للإعارة.
     * * @return معرف الإعارة (String).
     */
    public String getLoanId() { return loanId; }

    /**
     * استرجاع الوسيط المُعار.
     * * @return كائن الوسيط (Media).
     */
    public Media getMedia() { return media; }

    /**
     * استرجاع المستخدم المُعير.
     * * @return كائن المستخدم (User).
     */
    public User getUser() { return user; }

    /**
     * استرجاع تاريخ الإعارة.
     * * @return تاريخ الإعارة (LocalDate).
     */
    public LocalDate getBorrowDate() { return borrowDate; }

    /**
     * استرجاع تاريخ الاستحقاق.
     * * @return تاريخ الاستحقاق (LocalDate).
     */
    public LocalDate getDueDate() { return dueDate; }
}