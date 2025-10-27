package org.library.Service.Strategy;

import org.library.Domain.Loan;
import org.library.Domain.Media;
import org.library.Domain.User;
import org.library.Domain.Book;
import org.library.Service.Strategy.BookService; // يستخدم لجلب كائن Book
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanFileHandler {

    private static final String LOANS_FILE = "loans.txt";

    public static List<Loan> loadAllLoans() {
        List<Loan> loans = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                if (parts.length == 5) {
                    try {
                        String loanId = parts[0];
                        String mediaIsbn = parts[1];
                        String userId = parts[2];
                        LocalDate borrowDate = LocalDate.parse(parts[3]);
                        LocalDate dueDate = LocalDate.parse(parts[4]);

                        User user = findUserById(userId);
                        Media media = findMediaByIsbn(mediaIsbn);

                        if (user != null && media != null) {
                            loans.add(new Loan(loanId, media, user, borrowDate, dueDate));
                        }
                    } catch (Exception e) {
                        System.err.println("❌ خطأ في تحليل سطر الإعارة: " + line + ". " + e.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) { }
        catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف الإعارات: " + e.getMessage());
        }
        return loans;
    }

    public static void saveLoan(Loan loan) {
        try (FileWriter writer = new FileWriter(LOANS_FILE, true);
             PrintWriter printWriter = new PrintWriter(writer)) {

            // التنسيق: LoanID,MediaISBN,UserID,BorrowDate,DueDate
            String loanData = String.format("%s,%s,%s,%s,%s",
                    loan.getMedia(),
                    loan.getMedia().getIsbn(),
                    loan.getUser().getId(),
                    loan.getBorrowDate().toString(),
                    loan.getDueDate().toString());

            printWriter.println(loanData);
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ الإعارة في الملف: " + e.getMessage());
        }
    }

    public static void rewriteAllLoans(List<Loan> loans) {
        try (FileWriter writer = new FileWriter(LOANS_FILE, false);
             PrintWriter printWriter = new PrintWriter(writer)) {

            for (Loan loan : loans) {
                String loanData = String.format("%s,%s,%s,%s,%s",
                        loan.getMedia(),
                        loan.getMedia().getIsbn(),
                        loan.getUser().getId(),
                        loan.getBorrowDate().toString(),
                        loan.getDueDate().toString());
                printWriter.println(loanData);
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء إعادة كتابة ملف الإعارات: " + e.getMessage());
        }
    }

    /** دالة مساعدة للبحث عن كائن Media (كتاب) بناءً على ISBN. */
    private static Media findMediaByIsbn(String isbn) {
        // يتم إنشاء BookService مؤقتاً للبحث في ملف الكتب
        List<Book> allBooks = new BookService().searchBooks(isbn);
        // يتم تصفية النتائج للبحث عن التطابق التام بالـ ISBN
        Optional<Book> result = allBooks.stream().filter(b -> b.getIsbn().equals(isbn)).findFirst();
        return result.orElse(null);
    }

    /** دالة مساعدة للبحث عن المستخدم بواسطة ID. */
    private static User findUserById(String userId) {
        return UserFileHandler.loadAllUsers().stream()
                .filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
    }
}