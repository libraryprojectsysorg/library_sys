/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */

package org.library.Service.strategy;

import org.library.domain.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LoanFileHandler {

    private static final Logger logger = Logger.getLogger(LoanFileHandler.class.getName());
    private static String LOANS_FILE = "loans.txt";


    public static void setLoansFile(String filePath) {
        LOANS_FILE = filePath;
    }

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
                        logger.log(Level.WARNING, "خطأ في تحليل السطر: " + line, e);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "خطأ في قراءة الملف: " + e.getMessage(), e);
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
            logger.log(Level.SEVERE, "خطأ في حفظ الإعارة: " + e.getMessage(), e);
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
            logger.log(Level.SEVERE, "خطأ في إعادة كتابة الملف: " + e.getMessage(), e);
        }
    }

    private Media findMediaByIsbn(String isbn) {
        BookCDService bookCDService = new BookCDService();


        List<Book> books = bookCDService.searchBooks(isbn);
        Optional<Book> foundBook = books.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst();
        if (foundBook.isPresent()) {
            return foundBook.get();
        }


        List<CD> cds = bookCDService.searchCD(isbn);
        Optional<CD> foundCD = cds.stream()
                .filter(cd -> cd.getIsbn().equals(isbn))
                .findFirst();

        return foundCD.orElse(null);
    }


    private User findUserById(String userId) {
        List<User> users = UserFileHandler.loadAllUsers();
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }



    public boolean isMediaBorrowed(String mediaIsbn) {
        List<Loan> loans = loadAllLoans();
        for (Loan loan : loans) {

            if (loan.getMedia().getIsbn().equals(mediaIsbn) && !loan.isReturned()) {
                return true;
            }
        }
        return false;
    }

}