package org.example;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Main entry point for Library Management System (Phase 1).
 * Demonstrates admin login with retry (Sprint 1), borrowing (Sprint 2), and reminders (Sprint 3).
 *
 * @author YourName
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthAdmin authAdmin = new AuthAdmin();

        // Demo data for testing overdue (Sprint 2 & 3)
        setupDemoData(authAdmin);

        System.out.println("=== Library Management System (Phase 1) ===");

        // Login retry loop (until success or exit)
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter admin email: ");
            String email = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();

            if (authAdmin.login(email, password)) {
                System.out.println(authAdmin.getErrorMessage());  // "Login successful"
                loggedIn = true;
                authAdmin.showAdminMenu();  // Enters CLI menu: Send Reminders (US3.1), Logout
            } else {
                System.out.println(authAdmin.getErrorMessage());  // "Invalid credentials - please try again."
                System.out.print("Try again? (y/n): ");
                String retry = scanner.nextLine().toLowerCase();
                if (!retry.equals("y")) {
                    System.out.println("Exiting...");
                    break;  // Exit if no retry
                }
            }
        }

        scanner.close();
        System.out.println("System exited.");
    }

    /**
     * Setup demo data for testing (e.g., overdue loan for reminders).
     *
     * @param authAdmin to access borrowService
     */
    private static void setupDemoData(AuthAdmin authAdmin) {
        BorrowService borrowService = authAdmin.borrowService;  // Package-private access

        // Create demo user and book
        User demoUser = new User("U001", "Demo User", "demo@example.com");
        Book demoBook = new Book("Demo Book", "Demo Author", "123456789");

        // Borrow book 30 days ago (overdue >28 days, for US2.2 & US3.1)
        LocalDate oldBorrowDate = LocalDate.now().minusDays(30);  // 30 days ago
        LocalDate dueDate = oldBorrowDate.plusDays(28);  // Due 2 days ago
        String loanId = "DEMO_LOAN";
        Loan demoLoan = new Loan(loanId, demoBook, demoUser, oldBorrowDate, dueDate);
        borrowService.addLoan(demoLoan);  // Use method
        demoBook.setAvailable(false);  // Mark borrowed

        System.out.println("Demo data loaded: 1 overdue book for testing reminders.");
    }
}