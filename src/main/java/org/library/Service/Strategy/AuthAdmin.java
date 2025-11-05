package org.library.Service.Strategy;

import org.library.Domain.*;
import org.library.Service.Strategy.fines.FineCalculator;
import java.util.List;
import java.util.Scanner;
import io.github.cdimascio.dotenv.Dotenv;

public class AuthAdmin {

    private final List<User> users;
    private boolean isLoggedIn = false;
    private String loggedInEmail = null;
    private Role loggedInRole = null;

    private final BookService bookService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;

    private static final Dotenv dotenv = Dotenv.load();
    private static final String SUPER_ADMIN_EMAIL = dotenv.get("ADMIN_EMAIL");
    private static final String SUPER_ADMIN_PASS = dotenv.get("ADMIN_PASS");

    public enum Role { SUPER_ADMIN, ADMIN ,USER}

    public AuthAdmin(BorrowService borrowService, ReminderService reminderService, FineCalculator fineCalculator, BookService bookService) {
        if (UserFileHandler.getUserByCredentials(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASS) == null) {
            UserFileHandler.saveUser(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASS, "SUPER_ADMIN", "SA001", "Library Super Admin");
        }
        this.users = UserFileHandler.loadAllUsers();
        this.borrowService = borrowService;
        this.reminderService = reminderService;
        this.fineCalculator = fineCalculator;
        this.bookService = bookService;
    }

    public boolean login(String email, String password) {
        User user = UserFileHandler.getUserByCredentials(email, password);
        if (user != null) {
            isLoggedIn = true;
            loggedInEmail = email;
            loggedInRole = user.getRole().equalsIgnoreCase("SUPER_ADMIN") ? Role.SUPER_ADMIN
                    : user.getRole().equalsIgnoreCase("ADMIN") ? Role.ADMIN
              : user.getRole().equalsIgnoreCase("USER") ? Role.USER : null;
            return true;
        }
        return false;
    }
    public boolean isLoggedInAdmin() {
        return isLoggedIn && (loggedInRole == Role.SUPER_ADMIN || loggedInRole == Role.ADMIN);
    }

    public boolean isSuperAdmin() {
        return isLoggedIn && loggedInRole == Role.SUPER_ADMIN;
    }
    public boolean isLoggedInUser() {
        return isLoggedIn && (loggedInRole == Role.USER || loggedInRole == Role.USER);
    }
    public String getErrorMessage() {
        if (!isLoggedIn) return "Invalid credentials - please try again.";
        return "Login successful";
    }

    public void logout() {
        isLoggedIn = false;
        loggedInEmail = null;
        loggedInRole = null;
    }

    public void showAdminMenu(Scanner scanner) {
        if (!isLoggedInAdmin()) {
            System.out.println("❌ هذه القائمة مخصصة للمدراء فقط.");
            return;
        }

        while (isLoggedIn) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Add Book");
            System.out.println("2. Delete Book");
            System.out.println("3. View All Books");
            System.out.println("4. Add CD");
            System.out.println("5. Delete CD");
            System.out.println("6. View All CDs");
            System.out.println("7. Send Overdue Reminders");

            if (isSuperAdmin()) {
                System.out.println("8. Add Admin");
                System.out.println("9. Delete Admin");
                System.out.println("10. Unregister User");
                System.out.println("11. Fine Summary");
                System.out.println("12. Logout");
            } else {
                System.out.println("8. Fine Summary");
                System.out.println("9. Logout");
            }

            System.out.print("Choose option: ");
            int choice;
            try { choice = Integer.parseInt(scanner.nextLine().trim()); }
            catch (Exception e) { System.out.println("Invalid input."); continue; }

            if (isSuperAdmin()) handleSuperAdminChoice(choice, scanner);
            else handleAdminChoice(choice, scanner);
        }
    }

    private void handleSuperAdminChoice(int choice, Scanner scanner) {
        switch (choice) {
            case 1 -> addBookInteractive(scanner);
            case 2 -> deleteBookInteractive(scanner);
            case 3 -> viewAllBooks();
            case 4 -> addCDInteractive(scanner);
            case 5 -> deleteCDInteractive(scanner);
            case 6 -> viewAllCDs();
            case 7 -> sendOverdueRemindersInteractive(scanner);
            case 8 -> addAdminInteractive(scanner);
            case 9 -> deleteAdminInteractive(scanner);
            case 10 -> unregisterUserInteractive(scanner);
            case 11 -> fineSummaryInteractive(scanner);
            case 12 -> { logout(); System.out.println("Logged out."); }
            default -> System.out.println("Invalid option.");
        }
    }

    private void handleAdminChoice(int choice, Scanner scanner) {
        switch (choice) {
            case 1 -> addBookInteractive(scanner);
            case 2 -> deleteBookInteractive(scanner);
            case 3 -> viewAllBooks();
            case 4 -> addCDInteractive(scanner);
            case 5 -> deleteCDInteractive(scanner);
            case 6 -> viewAllCDs();
            case 7 -> sendOverdueRemindersInteractive(scanner);
            case 8 -> fineSummaryInteractive(scanner);
            case 9 -> { logout(); System.out.println("Logged out."); }
            default -> System.out.println("Invalid option.");
        }

    }

    // ======== العمليات ========
    private void addAdminInteractive(Scanner scanner) {
        System.out.print("Email: "); String email = scanner.nextLine().trim();
        System.out.print("Password: "); String pass = scanner.nextLine().trim();
        System.out.print("ID: "); String id = scanner.nextLine().trim();
        System.out.print("Full Name: "); String name = scanner.nextLine().trim();
        boolean added = UserFileHandler.saveUser(email, pass, "ADMIN", id, name);
        System.out.println(added ? "✅ Admin added!" : "❌ Failed to add admin.");
    }

    private void deleteAdminInteractive(Scanner scanner) {
        System.out.print("Enter Admin ID to delete: "); String id = scanner.nextLine().trim();
        User user = findUserById(id);
        if (user == null || !user.getRole().equalsIgnoreCase("ADMIN")) { System.out.println("❌ Not found."); return; }
        boolean removed = UserFileHandler.removeUserById(id, loggedInRole.name());
        System.out.println(removed ? "✅ Admin deleted." : "❌ Failed.");
        users.remove(user);
    }

    private void unregisterUserInteractive(Scanner scanner) {
        System.out.print("Enter user ID to unregister: ");
        String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null || user.getRole().equalsIgnoreCase("SUPER_ADMIN") || user.getRole().equalsIgnoreCase("ADMIN")) {
            System.out.println("❌ Cannot unregister this user."); return;
        }
        if (borrowService.hasActiveLoans(user) || user.hasUnpaidFines()) {
            System.out.println("⚠️ Cannot unregister: active loans or unpaid fines."); return;
        }
        borrowService.unregisterUser(userId);
        boolean removedFromFile = UserFileHandler.removeUserById(userId, loggedInRole.name());
        System.out.println(removedFromFile ? "✅ User unregistered." : "❌ Failed.");
        users.remove(user);
    }

    private void fineSummaryInteractive(Scanner scanner) {
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("User not found."); return; }
        FineFileManager.loadFines(user);
        int total = fineCalculator.calculateTotalFine(user);
        System.out.println("إجمالي الغرامات المستحقة: " + total + " NIS.");
    }

    private void sendOverdueRemindersInteractive(Scanner scanner) {
        System.out.print("Enter user email to send reminder: ");
        String email = scanner.nextLine().trim();
        User targetUser = users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (targetUser == null) { System.out.println("❌ User not found."); return; }
        FineFileManager.loadFines(targetUser);
        int totalFine = fineCalculator.calculateTotalFine(targetUser);
        if (totalFine == 0) { System.out.println("✅ No fines."); return; }
        String message = "يجب عليك دفع الغرامة المالية التي قدرها " + totalFine + " شيكل.";
        new EmailNotifier(new RealEmailServer()).notify(targetUser, message);
        System.out.println("✅ Email sent.");
    }

    // ======== Books & CDs ========
    public void addBookInteractive(Scanner scanner) {
        System.out.print("Book title: "); String title = scanner.nextLine().trim();
        System.out.print("Author: "); String author = scanner.nextLine().trim();
        System.out.print("ISBN: "); String isbn = scanner.nextLine().trim();
        boolean added = bookService.addBook(title, author, isbn);
        System.out.println(added ? "✅ Book added!" : "⚠️ Already exists.");
    }

    public void deleteBookInteractive(Scanner scanner) {
        System.out.print("ISBN to delete: "); String isbn = scanner.nextLine().trim();
        boolean removed = bookService.removeByIsbn(isbn);
        System.out.println(removed ? "✅ Book deleted." : "❌ Not found.");
    }

    public void viewAllBooks() {
        List<Book> all = bookService.searchBooks("");
        if (all.isEmpty()) { System.out.println("📚 No books."); return; }
        all.forEach(b -> System.out.println("- " + b.getTitle() + " by " + b.getAuthor() + " (ISBN: " + b.getIsbn() + ")"));
    }

    public void addCDInteractive(Scanner scanner) {
        System.out.print("CD code: "); String code = scanner.nextLine().trim();
        System.out.print("CD title: "); String title = scanner.nextLine().trim();
        System.out.print("CD author/artist: "); String author = scanner.nextLine().trim();
        boolean added = CDFileHandler.saveCD(new CD(title, author, code));
        System.out.println(added ? "✅ CD added!" : "⚠️ Already exists.");
    }

    public void deleteCDInteractive(Scanner scanner) {
        System.out.print("CD code to delete: "); String code = scanner.nextLine().trim();
        boolean removed = CDFileHandler.removeCDByCode(code);
        System.out.println(removed ? "✅ CD deleted!" : "❌ Not found.");
    }

    public void viewAllCDs() {
        List<CD> allCDs = CDFileHandler.loadAllCDs();
        if (allCDs.isEmpty()) { System.out.println("📀 No CDs."); return; }
        allCDs.forEach(cd -> System.out.println("- " + cd.getTitle() + " by " + cd.getAuthor() + " (Code: " + cd.getIsbn() + ")"));
    }

    private User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }
}
