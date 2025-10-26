package org.library.ui;

import org.library.Domain.Admin;
import org.library.Service.Strategy.BorrowService;

import org.library.Service.Strategy.fines.FineCalculator;
import org.library.Service.Strategy.ReminderService;
import org.library.Domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * وحدة التحكم الخاصة بالمسؤول (Admin Console Controller).
 * تنتمي إلى طبقة العرض (Presentation Layer) وتتعامل مع الإدخال والإخراج.
 *
 * @author YourName
 * @version 1.1
 */
public class AuthAdmin {
    private final List<Admin> admins = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private boolean isLoggedIn = false;


    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;

    /**
     * منشئ (Constructor) يقوم بـ حقن التبعيات (Dependency Injection).
     *
     * @param borrowService خدمة الإعارة ومنطق التأخير.
     * @param reminderService خدمة إرسال التذكيرات (Observer Integration).
     * @param fineCalculator خدمة حساب الغرامات الإجمالية.
     */
    public AuthAdmin(BorrowService borrowService, ReminderService reminderService, FineCalculator fineCalculator) {

        admins.add(new Admin("s12217663@stu.najah.edu", "ws1234"));


        users.add(new User("U001", "Demo User", "demo@example.com"));


        this.borrowService = borrowService;
        this.reminderService = reminderService;
        this.fineCalculator = fineCalculator;
    }


    /**
     * Login method for admin (US1.1).
     *
     * @param email the admin email
     * @param password the admin password
     * @return true if login successful, false otherwise
     */
    public boolean login(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            return false;
        }
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(password)) {
                isLoggedIn = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Get error/success message after login attempt.
     *
     * @return error or success message
     */
    public String getErrorMessage() {
        if (!isLoggedIn) {
            return "Invalid credentials - please try again.";
        }
        return "Login successful";
    }

    /**
     * Logout method (US1.2).
     */
    public void logout() {
        isLoggedIn = false;
    }

    /**
     * Check if admin is logged in.
     *
     * @return true if logged in
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Admin console menu (Sprint 3-5 integration).
     * Call this after successful login. Provides CLI for admin actions.
     * Handles US3.1 reminders, US4.2 unregister, US5.3 fine summary.
     */
    public void showAdminMenu() {
        Scanner scanner = new Scanner(System.in);
        while (isLoggedIn) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Send Overdue Reminders (US3.1)");
            System.out.println("2. Logout (US1.2)");
            System.out.println("3. Unregister User (US4.2)");
            System.out.println("4. Fine Summary (US5.3)");
            System.out.print("Choose option: ");

            int choice;
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("No input. Exiting menu.");
                    break;
                }
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:

                    reminderService.sendReminders();
                    System.out.println("Reminders sent to overdue users!");
                    break;
                case 2:
                    logout();
                    System.out.println("Logged out successfully.");
                    break;
                case 3: // US4.2 Unregister user
                    System.out.print("Enter user ID to unregister: ");
                    if (!scanner.hasNextLine()) {
                        System.out.println("No input. Skipping.");
                        break;
                    }
                    String userId = scanner.nextLine().trim();
                    User user = findUserById(userId);
                    if (user == null) {
                        System.out.println("User not found.");
                        break;
                    }
                    if (!isLoggedIn()) {
                        System.out.println("Only admins can unregister.");
                        break;
                    }
                    if (hasActiveLoans(user) || user.hasUnpaidFines()) {
                        System.out.println("Users with active loans or unpaid fines cannot be unregistered.");
                        break;
                    }

                    boolean removedLoans = borrowService.unregisterUser(userId);
                    if (removedLoans || !borrowService.getLoans().stream().anyMatch(loan -> loan.getUser().getId().equals(userId))) {
                        users.remove(user);
                        System.out.println("User unregistered successfully.");
                    } else {
                        System.out.println("Error unregistering user: No loans to remove.");
                    }
                    break;
                case 4: // US5.3 Fine summary (mixed media)
                    System.out.print("Enter user ID for fine summary: ");
                    if (!scanner.hasNextLine()) {
                        System.out.println("No input. Skipping.");
                        break;
                    }
                    userId = scanner.nextLine().trim();
                    user = findUserById(userId);
                    if (user == null) {
                        System.out.println("User not found.");
                        break;
                    }

                    int total = fineCalculator.calculateTotalFine(user);
                    System.out.println("Fine summary: " + total + " NIS (accurate across media types).");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }

    /**
     * Find user by ID (Helper for admin menu).
     * @param id the user ID
     * @return the user or null
     */
    private User findUserById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if user has active loans (Helper for US4.2).
     * @param user the user
     * @return true if has active
     */
    private boolean hasActiveLoans(User user) {
        return borrowService.getLoans().stream()
                .anyMatch(loan -> loan.getUser().equals(user));
    }
}