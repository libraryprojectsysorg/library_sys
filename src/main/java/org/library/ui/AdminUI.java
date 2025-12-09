/*package org.library.ui;

import org.library.Domain.*;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminUI {

    // ===== Fields =====
    private final AuthAdmin authAdmin;
    private final Scanner scanner;

    private List<User> users = new ArrayList<>();
    private boolean isLoggedIn = false;
    private String loggedInEmail;
    private Role loggedInRole;

    private final BookCDService bookCDService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;

    public enum Role { SUPER_ADMIN, ADMIN, USER }

    // ===== Constructor =====
    public AdminUI(AuthAdmin authAdmin, Scanner scanner, BookCDService bookCDService,
                   BorrowService borrowService, ReminderService reminderService,
                   FineCalculator fineCalculator) {
        this.authAdmin = authAdmin;
        this.scanner = scanner;
        this.bookCDService = bookCDService;
        this.borrowService = borrowService;
        this.reminderService = reminderService;
        this.fineCalculator = fineCalculator;
        loadUsers();
    }

    // ===== Initialization =====
    public void loadUsers() {
        users.clear();
        users.addAll(UserFileHandler.loadAllUsers());
    }

    // ===== Authentication =====
    public boolean login(String email, String password) {
        User user = UserFileHandler.getUserByCredentials(email, password);
        if (user != null) {
            isLoggedIn = true;
            loggedInEmail = email;
            loggedInRole = switch (user.getRole().toUpperCase()) {
                case "SUPER_ADMIN" -> Role.SUPER_ADMIN;
                case "ADMIN" -> Role.ADMIN;
                case "USER" -> Role.USER;
                default -> null;
            };
            return true;
        }
        return false;
    }

    public boolean isSuperAdmin() { return isLoggedIn && loggedInRole == Role.SUPER_ADMIN; }
    public boolean isLoggedInAdmin() { return isLoggedIn && (loggedInRole == Role.SUPER_ADMIN || loggedInRole == Role.ADMIN); }
    public boolean isLoggedInUser() { return isLoggedIn && loggedInRole == Role.USER; }

    public void logout() {
        isLoggedIn = false;
        loggedInEmail = null;
        loggedInRole = null;
        authAdmin.logout();
    }

    public User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    // ===== Menu =====
    public void showAdminMenu() {

        while (authAdmin.isLoggedInAdmin() || authAdmin.isSuperAdmin()) {
            printMenu();
            int choice = readIntInput("Choose option: ");
            if (isSuperAdmin()) handleSuperAdminChoice(choice);
            else handleAdminChoice(choice);
        }
    }

    public void printMenu() {
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
            System.out.println("12. Borrow Book");
            System.out.println("13. Return Book");
            System.out.println("14. Borrow CD");
            System.out.println("15. Return CD");
            System.out.println("16. Logout");
        } else {
            System.out.println("8. Fine Summary");
            System.out.println("9. Borrow Book");
            System.out.println("10. Return Book");
            System.out.println("11. Borrow CD");
            System.out.println("12. Return CD");
            System.out.println("13. Pay Fine");
            System.out.println("14. Logout");
        }

    }

    private int readIntInput(String message) {
        System.out.print(message);
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    // ===== Handle Choices =====
    private void handleSuperAdminChoice(int choice) {
        switch (choice) {
            case 1 -> addBookInteractive();
            case 2 -> deleteBookInteractive();
            case 3 -> viewAllBooks();
            case 4 -> addCDInteractive();
            case 5 -> deleteCDInteractive();
            case 6 -> viewAllCDs();
            case 7 -> sendOverdueRemindersInteractive();
            case 8 -> addAdminInteractive();
            case 9 -> deleteAdminInteractive();
            case 10 -> unregisterUserInteractive();
            case 11 -> fineSummaryInteractive();
            case 12 -> borrowBookInteractive();
            case 13 -> returnBookInteractive();
            case 14 -> borrowCDInteractive();
            case 15 -> returnCDInteractive();
            case 16 -> { logout(); System.out.println("Logged out."); }
            default -> System.out.println("Invalid option.");
        }
    }

    private void handleAdminChoice(int choice) {
        switch (choice) {
            case 1 -> addBookInteractive();
            case 2 -> deleteBookInteractive();
            case 3 -> viewAllBooks();
            case 4 -> addCDInteractive();
            case 5 -> deleteCDInteractive();
            case 6 -> viewAllCDs();
            case 7 -> sendOverdueRemindersInteractive();
            case 8 -> fineSummaryInteractive();
            case 9 -> borrowBookInteractive();
            case 10 -> returnBookInteractive();
            case 11 -> borrowCDInteractive();
            case 12 -> returnCDInteractive();
            case 13 -> payFineForUserInteractive();
            case 14 -> { logout(); System.out.println("Logged out."); }
            default -> System.out.println("Invalid option.");
        }
    }

    // ===== Book & CD Operations =====
    private void addBookInteractive() {
        System.out.print("Book title: "); String title = scanner.nextLine().trim();
        System.out.print("Author: "); String author = scanner.nextLine().trim();
        System.out.print("ISBN: "); String isbn = scanner.nextLine().trim();
        boolean added = bookCDService.addBook(title, author, isbn);
        System.out.println(added ? "✅ Book added!" : "⚠ Already exists.");
    }

    private void deleteBookInteractive() {
        System.out.print("ISBN to delete: "); String isbn = scanner.nextLine().trim();
        boolean removed = bookCDService.removeByIsbn(isbn);
        System.out.println(removed ? "✅ Book deleted." : "❌ Not found.");
    }

    private void viewAllBooks() {
        List<Book> all = bookCDService.searchBooks("");
        if (all.isEmpty()) { System.out.println("📚 No books."); return; }
        all.forEach(b -> System.out.println("- " + b.getTitle() + " by " + b.getAuthor() + " (ISBN: " + b.getIsbn() + ")"));
    }

    private void addCDInteractive() {
        System.out.print("CD title: "); String title = scanner.nextLine().trim();
        System.out.print("CD author/artist: "); String author = scanner.nextLine().trim();
        System.out.print("CD code: "); String code = scanner.nextLine().trim();
        boolean added = bookCDService.addCD(title, author, code);
        System.out.println(added ? "✅ CD added!" : "⚠ Already exists.");
    }

    private void deleteCDInteractive() {
        System.out.print("CD code to delete: "); String code = scanner.nextLine().trim();
        boolean removed = bookCDService.removeCDByCode(code);
        System.out.println(removed ? "✅ CD deleted!" : "❌ Not found.");
    }

    private void viewAllCDs() {
        List<CD> all = CDFileHandler.loadAllCDs();
        if (all.isEmpty()) { System.out.println("📀 No CDs."); return; }
        all.forEach(cd -> System.out.println("- " + cd.getTitle() + " by " + cd.getAuthor() + " (Code: " + cd.getIsbn() + ")"));
    }

    // ===== Borrow & Return =====
    private void borrowBookInteractive() {
        System.out.print("Book title to borrow: "); String title = scanner.nextLine().trim();
        List<Book> books = bookCDService.searchBooks(title);
        if (books.isEmpty()) { System.out.println("❌ Book not found."); return; }
        Book book = books.get(0);

        System.out.print("User ID: "); String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("❌ User not found."); return; }

        try { borrowService.borrowMedia(book, user); System.out.println("✅ Borrowed successfully: " + user.getName()); }
        catch (RuntimeException e) { System.out.println("❌ Failed: " + e.getMessage()); }
    }

    private void returnBookInteractive() {
        System.out.print("Book title to return: "); String title = scanner.nextLine().trim();
        List<Loan> loans = borrowService.getLoans().stream()
                .filter(l -> l.getMedia() instanceof Book && ((Book) l.getMedia()).getTitle().equalsIgnoreCase(title))
                .toList();
        if (loans.isEmpty()) { System.out.println("❌ No loans."); return; }
        Loan loan = loans.get(0);

        int fine = borrowService.returnMedia(loan.getLoanId());
        System.out.println("✅ Returned: " + ((Book) loan.getMedia()).getTitle());
        if (fine > 0) System.out.println("⚠ Fine: " + fine + " NIS on " + loan.getUser().getName());
    }

    private void borrowCDInteractive() {
        System.out.print("CD title to borrow: "); String title = scanner.nextLine().trim();
        List<CD> cds = bookCDService.searchCD(title);
        if (cds.isEmpty()) { System.out.println("❌ CD not found."); return; }
        CD cd = cds.get(0);

        System.out.print("User ID: "); String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("❌ User not found."); return; }

        try { borrowService.borrowMedia(cd, user); System.out.println("✅ Borrowed CD: " + user.getName()); }
        catch (RuntimeException e) { System.out.println("❌ Failed: " + e.getMessage()); }
    }

    private void returnCDInteractive() {
        System.out.print("CD code to return: "); String code = scanner.nextLine().trim();
        List<Loan> loans = borrowService.getLoans().stream()
                .filter(l -> l.getMedia() instanceof CD && ((CD) l.getMedia()).getIsbn().equalsIgnoreCase(code))
                .toList();
        if (loans.isEmpty()) { System.out.println("❌ No loans."); return; }
        Loan loan = loans.get(0);

        int fine = borrowService.returnMedia(loan.getLoanId());
        System.out.println("✅ Returned CD: " + ((CD) loan.getMedia()).getTitle());
        if (fine > 0) System.out.println("⚠ Fine: " + fine + " NIS on " + loan.getUser().getName());
    }

    // ===== Admin/User Operations =====
    private void addAdminInteractive() {
        System.out.print("Email: "); String email = scanner.nextLine().trim();
        System.out.print("Password: "); String pass = scanner.nextLine().trim();
        System.out.print("ID: "); String id = scanner.nextLine().trim();
        System.out.print("Full Name: "); String name = scanner.nextLine().trim();
        boolean added = UserFileHandler.saveUser(email, pass, "ADMIN", id, name);
        System.out.println(added ? "✅ Admin added!" : "❌ Failed to add admin.");
        if (added) loadUsers();
    }

    private void deleteAdminInteractive() {
        System.out.print("Admin ID to delete: "); String id = scanner.nextLine().trim();
        User user = findUserById(id);
        if (user == null || !user.getRole().equalsIgnoreCase("ADMIN")) { System.out.println("❌ Not found."); return; }
        boolean removed = UserFileHandler.removeUserById(id, loggedInRole.name());
        System.out.println(removed ? "✅ Admin deleted." : "❌ Failed.");
        if (removed) users.remove(user);
    }

    private void unregisterUserInteractive() {
        System.out.print("User ID to unregister: "); String id = scanner.nextLine().trim();
        User user = findUserById(id);
        if (user == null || user.getRole().equalsIgnoreCase("ADMIN") || user.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
            System.out.println("❌ Cannot unregister."); return;
        }
        if (borrowService.hasActiveLoans(user) || user.hasUnpaidFines()) {
            System.out.println("⚠ Active loans or unpaid fines."); return;
        }
        borrowService.unregisterUser(id);
        boolean removed = UserFileHandler.removeUserById(id, loggedInRole.name());
        System.out.println(removed ? "✅ User unregistered." : "❌ Failed.");
        if (removed) users.remove(user);
    }

    private void fineSummaryInteractive() {
        System.out.print("User ID: "); String id = scanner.nextLine().trim();
        User user = findUserById(id);
        if (user == null) { System.out.println("❌ Not found."); return; }
        FineFileManager.loadFines(user);
        int total = fineCalculator.calculateTotalFine(user);
        System.out.println("Total fines: " + total + " NIS.");
    }

    private void sendOverdueRemindersInteractive() {
        System.out.print("User email: "); String email = scanner.nextLine().trim();
        User user = users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (user == null) { System.out.println("❌ Not found."); return; }
        FineFileManager.loadFines(user);
        int total = fineCalculator.calculateTotalFine(user);
        if (total == 0) { System.out.println("✅ No fines."); return; }
        new EmailNotifier(new RealEmailServer()).notify(user, "You owe " + total + " NIS fine.");
        System.out.println("✅ Email sent.");
    }

    private void payFineForUserInteractive() {
        System.out.print("User email: "); String email = scanner.nextLine().trim();
        User user = users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (user == null) { System.out.println("❌ Not found."); return; }
        FineFileManager.loadFines(user);
        int fine = fineCalculator.calculateTotalFine(user);
        if (fine == 0) { System.out.println("✅ No fines."); return; }
        System.out.println("User " + user.getName() + " owes " + fine + " NIS.");
        System.out.print("Pay now? (y/n): "); String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("y")) {
            for (Fine f : user.getFines()) if (!f.isPaid()) user.payFine(f);
            FineFileManager.removePaidFines(user);
            System.out.println("✅ All fines paid.");
        } else System.out.println("Cancelled.");
    }
    public void updateRole(AuthAdmin authAdmin) {
        if (authAdmin.isSuperAdmin()) loggedInRole = Role.SUPER_ADMIN;
        else if (authAdmin.isLoggedInAdmin()) loggedInRole = Role.ADMIN;
        else loggedInRole = Role.USER;
        isLoggedIn = true;
    }
}*/
