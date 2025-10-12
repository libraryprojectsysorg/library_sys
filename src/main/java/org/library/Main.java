package org.library;

import org.library.Domain.Book;
import org.library.Domain.Loan;
import org.library.Domain.User;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.ReminderService;
import org.library.Service.Strategy.fines.FineCalculator;
import org.library.Service.Strategy.EmailNotifier;
import org.library.ui.AuthAdmin;
import org.library.Service.Strategy.RealEmailServer;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for Library Management System.
 * يطبق حقن التبعيات (DI) لإعداد النظام.
 *
 * @author YourName
 * @version 1.1
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        RealEmailServer realEmailServer = new RealEmailServer();
        EmailNotifier emailNotifier = new EmailNotifier(realEmailServer);


        BorrowService borrowService = new BorrowService(emailNotifier);


        ReminderService reminderService = new ReminderService(List.of(emailNotifier), borrowService);


        FineCalculator fineCalculator = new FineCalculator(borrowService);


        AuthAdmin authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator);


        setupDemoData(borrowService);

        System.out.println("=== Library Management System (Phase 1) ===");


        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter admin email: ");
            String email = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();

            if (authAdmin.login(email, password)) {
                System.out.println(authAdmin.getErrorMessage());
                loggedIn = true;
                authAdmin.showAdminMenu();
            } else {
                System.out.println(authAdmin.getErrorMessage());
                System.out.print("Try again? (y/n): ");
                String retry = scanner.nextLine().toLowerCase();
                if (!retry.equals("y")) {
                    System.out.println("Exiting...");
                    break;
                }
            }
        }

        scanner.close();
        System.out.println("System exited.");
    }

    /**
     * Setup demo data for testing (e.g., overdue loan for reminders).
     *
     * @param borrowService لاستخدامها مباشرة لإضافة القروض
     */
    private static void setupDemoData(BorrowService borrowService) {



        User demoUser = new User("U001", "Demo User", "demo@example.com");

        Book demoBook = new Book("Demo Book", "Demo Author", "123456789");


        LocalDate oldBorrowDate = LocalDate.now().minusDays(30);
        LocalDate dueDate = oldBorrowDate.plusDays(28);
        String loanId = "DEMO_LOAN";
        Loan demoLoan = new Loan(loanId, demoBook, demoUser, oldBorrowDate, dueDate);
        borrowService.addLoan(demoLoan);
        demoBook.setAvailable(false);

        System.out.println("Demo data loaded: 1 overdue book for testing reminders.");
    }
}