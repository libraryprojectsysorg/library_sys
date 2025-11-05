/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */

package org.library.Service.Strategy;

import org.library.Domain.*;
import org.library.Service.Strategy.BookService;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanFileHandler {

    private static final String LOANS_FILE = "loans.txt";

    public List<Loan> loadAllLoans() {
        List<Loan> loans = new ArrayList<>();
        File file = new File(LOANS_FILE);
        if (!file.exists()) {
            return loans;
        }
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

                        Media media = findMediaByIsbn(mediaIsbn);
                        User user = findUserById(userId);

                        if (user != null && media != null) {
                            loans.add(new Loan(loanId, media, user, borrowDate, dueDate));
                        }
                    } catch (Exception e) {
                        System.err.println("خطأ في تحليل السطر: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("خطأ في قراءة الملف: " + e.getMessage());
        }
        return loans;
    }

    public void saveLoan(Loan loan) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOANS_FILE, true))) {
            String line = String.format("%s,%s,%s,%s,%s",
                    loan.getLoanId(),
                    loan.getMedia().getIsbn(),
                    loan.getUser().getId(),
                    loan.getBorrowDate(),
                    loan.getDueDate());
            writer.println(line);
        } catch (IOException e) {
            System.err.println("خطأ في حفظ الإعارة: " + e.getMessage());
        }
    }

    public void rewriteAllLoans(List<Loan> loans) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOANS_FILE))) {
            for (Loan loan : loans) {
                String line = String.format("%s,%s,%s,%s,%s",
                        loan.getLoanId(),
                        loan.getMedia().getIsbn(),
                        loan.getUser().getId(),
                        loan.getBorrowDate(),
                        loan.getDueDate());
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("خطأ في إعادة كتابة الملف: " + e.getMessage());
        }
    }

    private Media findMediaByIsbn(String isbn) {
        BookService bookService = new BookService();
        List<Book> books = bookService.searchBooks(isbn);
        Optional<Book> result = books.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst();
        return result.orElse(null);
    }

    private User findUserById(String userId) {
        List<User> users = UserFileHandler.loadAllUsers(); // افترض إن عندك UserFileHandler
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}