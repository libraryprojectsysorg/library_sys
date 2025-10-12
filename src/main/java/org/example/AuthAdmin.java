package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Authentication and menu for Admin (Sprint 1 & 3-5).
 * Handles login/logout (US1.1, US1.2), reminders (US3.1), unregister (US4.2), fine summary (US5.3).
 *
 * @author YourName
 * @version 1.0
 */
public class AuthAdmin {
    private List<Admin> admins = new ArrayList<>();
    private List<User> users = new ArrayList<>();  // In-memory users for Sprint 4 (US4.2)
    private boolean isLoggedIn = false;
    BorrowService borrowService = new BorrowService();  // Package-private for Main access

    /**
     * Default constructor: Initializes admin and demo users list.
     */
    public AuthAdmin() {
        admins.add(new Admin("s12217663@stu.najah.edu", "ws1234"));  // Your custom admin
        // Demo user for testing (Sprint 4+)
        users.add(new User("U001", "Demo User", "demo@example.com"));
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
            return false;  // Edge case: invalid input (covers null/empty branches)
        }
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(password)) {
                isLoggedIn = true;
                return true;  // Valid → login success
            }
        }
        return false;  // No match → error
    }

    /**
     * Get error/success message after login attempt.
     *
     * @return error or success message
     */
    public String getErrorMessage() {
        if (!isLoggedIn) {
            return "Invalid credentials - please try again.";  // Branch for error
        }
        return "Login successful";  // Branch for success
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
        while (isLoggedIn) {  // Loop while logged in
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Send Overdue Reminders (US3.1)");
            System.out.println("2. Logout (US1.2)");
            System.out.println("3. Unregister User (US4.2)");
            System.out.println("4. Fine Summary (US5.3)");
            System.out.print("Choose option: ");

            int choice;
            try {
                if (!scanner.hasNextLine()) {  // إضافة لمنع NoSuchElement
                    System.out.println("No input. Exiting menu.");
                    break;
                }
                choice = scanner.nextInt();  // Read int
                scanner.nextLine();  // Consume newline
            } catch (Exception e) {  // Catch invalid input (e.g., letter 'a')
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();  // Clear the invalid line
                continue;  // Skip to next loop iteration
            }

            switch (choice) {
                case 1:
                    // Create ReminderService with Observer Pattern (Sprint 3)
                    EmailServer realServer = new RealEmailServer();  // Production server (prints messages)
                    EmailNotifier notifier = new EmailNotifier(realServer);
                    ReminderService reminderService = new ReminderService(List.of(notifier), borrowService);
                    reminderService.sendReminders();  // US3.1: Send "You have n overdue book(s)."
                    System.out.println("Reminders sent to overdue users!");
                    break;
                case 2:
                    logout();
                    System.out.println("Logged out successfully.");
                    break;
                case 3:  // US4.2 Unregister user
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
                    if (!isLoggedIn()) {  // Only admins
                        System.out.println("Only admins can unregister.");
                        break;
                    }
                    if (hasActiveLoans(user) || user.hasUnpaidFines()) {
                        System.out.println("Users with active loans or unpaid fines cannot be unregistered.");
                        break;
                    }
                    // إضافة: استدعاء unregisterUser لإزالة القروض من BorrowService
                    boolean removedLoans = borrowService.unregisterUser(userId);
                    if (removedLoans) {
                        users.remove(user);  // Remove from users list
                        System.out.println("User unregistered successfully.");
                    } else {
                        System.out.println("Error unregistering user: No loans to remove.");
                    }
                    break;
                case 4:  // US5.3 Fine summary (mixed media)
                    System.out.print("Enter user ID for fine summary: ");
                    if (!scanner.hasNextLine()) {  // إضافة للأمان
                        System.out.println("No input. Skipping.");
                        break;
                    }
                    userId = scanner.nextLine().trim();
                    user = findUserById(userId);
                    if (user == null) {
                        System.out.println("User not found.");
                        break;
                    }
                    FineCalculator calc = new FineCalculator(borrowService);
                    int total = calc.calculateTotalFine(user);
                    System.out.println("Fine summary: " + total + " NIS (accurate across media types).");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();  // Close outside loop (after exit)
    }

    /**
     * Find user by ID (Sprint 4 helper).
     * @param id the user ID
     * @return the user or null
     */
    private User findUserById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);  // Stream branch for coverage
    }

    /**
     * Check if user has active loans (US4.2 helper).
     * @param user the user
     * @return true if has active
     */
    private boolean hasActiveLoans(User user) {
        return borrowService.getLoans().stream()  // استخدم getLoans()
                .anyMatch(loan -> loan.getUser().equals(user));
    }
}