package org.library.Service.Strategy;

import org.library.Domain.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AuthAdmin {

    private final List<User> users;
    private boolean isLoggedIn = false;
    private String loggedInEmail = null;
    private Role loggedInRole = null;

    private final BookCDService bookCDService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;

    private static final String SUPER_ADMIN_EMAIL = System.getenv("ADMIN_EMAIL") != null ? System.getenv("ADMIN_EMAIL") : "default_super@library.com";
    private static final String SUPER_ADMIN_PASS = System.getenv("ADMIN_PASS") != null ? System.getenv("ADMIN_PASS") : "default_superpass123";

    public enum Role { SUPER_ADMIN, ADMIN, USER }

    public AuthAdmin(BorrowService borrowService, ReminderService reminderService,
                     FineCalculator fineCalculator, BookCDService bookCDService) {

        this.borrowService = borrowService;
        this.reminderService = reminderService;
        this.fineCalculator = fineCalculator;
        this.bookCDService = bookCDService;


        if (UserFileHandler.getUserByCredentials(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASS) == null) {
            UserFileHandler.saveUser(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASS, "SUPER_ADMIN", "SA001", "Library Super Admin");
        }


        this.users = new ArrayList<>();
    }


    public void loadUsers() {
        this.users.clear();
        this.users.addAll(UserFileHandler.loadAllUsers());
    }

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

    public boolean isLoggedInAdmin() {
        return isLoggedIn && (loggedInRole == Role.SUPER_ADMIN || loggedInRole == Role.ADMIN);
    }

    public boolean isSuperAdmin() {
        return isLoggedIn && loggedInRole == Role.SUPER_ADMIN;
    }

    public boolean isLoggedInUser() {
        return isLoggedIn && loggedInRole == Role.USER;
    }

    public String getErrorMessage() {
        return !isLoggedIn ? "Invalid credentials - please try again." : "Login successful";
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
            case 12 -> borrowBookInteractive(scanner);
            case 13 -> returnBookInteractive(scanner);
            case 14 -> borrowCDInteractive(scanner);
            case 15 -> returnCDInteractive(scanner);
            case 16 -> { logout(); System.out.println("Logged out."); }
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
            case 9 -> borrowBookInteractive(scanner);
            case 10 -> returnBookInteractive(scanner);
            case 11 -> borrowCDInteractive(scanner);
            case 12 -> returnCDInteractive(scanner);
            case 13 -> payFineForUserInteractive(scanner);
            case 14 -> { logout(); System.out.println("Logged out."); }
            default -> System.out.println("Invalid option.");
        }
    }

    // ======= User/Admin Operations =======

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
        System.out.print("Enter user ID to unregister: "); String userId = scanner.nextLine().trim();
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

    public void fineSummaryInteractive(Scanner scanner) {
        System.out.print("Enter user ID: "); String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("User not found."); return; }
        FineFileManager.loadFines(user);
        int total = fineCalculator.calculateTotalFine(user);
        System.out.println("إجمالي الغرامات المستحقة: " + total + " NIS.");
    }

    private void sendOverdueRemindersInteractive(Scanner scanner) {
        System.out.print("Enter user email to send reminder: "); String email = scanner.nextLine().trim();
        User targetUser = users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (targetUser == null) { System.out.println("❌ User not found."); return; }
        FineFileManager.loadFines(targetUser);
        int totalFine = fineCalculator.calculateTotalFine(targetUser);
        if (totalFine == 0) { System.out.println("✅ No fines."); return; }
        String message = "يجب عليك دفع الغرامة المالية التي قدرها " + totalFine + " شيكل.";
        new EmailNotifier(new RealEmailServer()).notify(targetUser, message);
        System.out.println("✅ Email sent.");
    }

    // ======= Book & CD Operations =======

    public void addBookInteractive(Scanner scanner) {
        System.out.print("Book title: "); String title = scanner.nextLine().trim();
        System.out.print("Author: "); String author = scanner.nextLine().trim();
        System.out.print("ISBN: "); String isbn = scanner.nextLine().trim();
        boolean added = bookCDService.addBook(title, author, isbn);
        System.out.println(added ? "✅ Book added!" : "⚠️ Already exists.");
    }

    public void deleteBookInteractive(Scanner scanner) {
        System.out.print("ISBN to delete: "); String isbn = scanner.nextLine().trim();
        boolean removed = bookCDService.removeByIsbn(isbn);
        System.out.println(removed ? "✅ Book deleted." : "❌ Not found.");
    }

    public void viewAllBooks() {
        List<Book> all = bookCDService.searchBooks("");
        if (all.isEmpty()) { System.out.println("📚 No books."); return; }
        all.forEach(b -> System.out.println("- " + b.getTitle() + " by " + b.getAuthor() + " (ISBN: " + b.getIsbn() + ")"));
    }

    public void addCDInteractive(Scanner scanner) {
        System.out.print("CD title: "); String title = scanner.nextLine().trim();
        System.out.print("CD author/artist: "); String author = scanner.nextLine().trim();
        System.out.print("CD code: "); String code = scanner.nextLine().trim();

        boolean added = bookCDService.addCD(title, author, code); // استخدام BookCDService
        System.out.println(added ? "✅ CD added!" : "⚠️ Already exists.");
    }


    public void deleteCDInteractive(Scanner scanner) {
        System.out.print("CD code to delete: "); String code = scanner.nextLine().trim();
        boolean removed = bookCDService.removeCDByCode(code);

        System.out.println(removed ? "✅ CD deleted!" : "❌ Not found.");
    }

    public void viewAllCDs() {
        List<CD> allCDs = CDFileHandler.loadAllCDs();
        if (allCDs.isEmpty()) { System.out.println("📀 No CDs."); return; }
        allCDs.forEach(cd -> System.out.println("- " + cd.getTitle() + " by " + cd.getAuthor() + " (Code: " + cd.getIsbn() + ")"));
    }

    // ======= Borrow & Return =======

  public void borrowBookInteractive(Scanner scanner) {
        System.out.println("\n=== استعارة كتاب ===");
        System.out.print("أدخل اسم الكتاب الذي تريد استعارته: "); String title = scanner.nextLine().trim();
        List<Book> matchingBooks = bookCDService.searchBooks(title);
        if (matchingBooks.isEmpty()) { System.out.println("❌ لم يتم العثور على كتاب بهذا الاسم."); return; }
        Book bookToBorrow = matchingBooks.get(0);
        System.out.print("أدخل ID الادمين الذي سيستعير الكتاب: "); String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("❌ الادمين غير موجود."); return; }
        try { borrowService.borrowMedia(bookToBorrow, user); System.out.println("✅ تم استعارة الكتاب بنجاح للأدمين: " + user.getName()); }
        catch (RuntimeException e) { System.out.println("❌ فشل الاستعارة: " + e.getMessage()); }
    }

    public void returnBookInteractive(Scanner scanner) {
        System.out.println("\n=== إرجاع كتاب ===");
        System.out.print("أدخل اسم الكتاب الذي تريد إرجاعه: "); String title = scanner.nextLine().trim();
        List<Loan> matchingLoans = borrowService.getLoans().stream()
                .filter(l -> l.getMedia() instanceof Book && ((Book) l.getMedia()).getTitle().equalsIgnoreCase(title))
                .toList();
        if (matchingLoans.isEmpty()) { System.out.println("❌ لا توجد إعارات لهذا الكتاب."); return; }
        Loan loanToReturn = matchingLoans.get(0);
        int fine = borrowService.returnMedia(loanToReturn.getLoanId());
        System.out.println("✅ تم إرجاع الكتاب بنجاح: " + ((Book) loanToReturn.getMedia()).getTitle());
        if (fine > 0) System.out.println("⚠️ تم فرض غرامة: " + fine + " NIS على الادمين: " + loanToReturn.getUser().getName());
    }

    public void borrowCDInteractive(Scanner scanner) {
        System.out.println("\n=== استعارة CD ===");
        System.out.print("أدخل اسم الـ CD الذي تريد استعارته: "); String title = scanner.nextLine().trim();
        List<CD> matchingCDs = bookCDService.searchCD(title);
        if (matchingCDs.isEmpty()) { System.out.println("❌ لم يتم العثور على CD بهذا الاسم."); return; }
        CD cdToBorrow = matchingCDs.get(0);
        System.out.print("أدخل ID الادمين الذي سيستعير الـ CD: "); String userId = scanner.nextLine().trim();
        User user = findUserById(userId);
        if (user == null) { System.out.println("❌ الادمين غير موجود."); return; }
        try { borrowService.borrowMedia(cdToBorrow, user); System.out.println("✅ تم استعارة الـ CD بنجاح للأدمين: " + user.getName()); }
        catch (RuntimeException e) { System.out.println("❌ فشل الاستعارة: " + e.getMessage()); }
    }

    public void returnCDInteractive(Scanner scanner) {
        System.out.println("\n=== إرجاع CD ===");
        System.out.print("أدخل كود الـ CD الذي تريد إرجاعه: "); String code = scanner.nextLine().trim();
        List<Loan> matchingLoans = borrowService.getLoans().stream()
                .filter(l -> l.getMedia() instanceof CD && ((CD) l.getMedia()).getIsbn().equalsIgnoreCase(code))
                .toList();
        if (matchingLoans.isEmpty()) { System.out.println("❌ لا توجد إعارات مطابقة للـ CD المدخل."); return; }
        Loan loanToReturn = matchingLoans.get(0);
        int fine = borrowService.returnMedia(loanToReturn.getLoanId());
        System.out.println("✅ تم إرجاع الـ CD بنجاح: " + ((CD) loanToReturn.getMedia()).getTitle());
        if (fine > 0) System.out.println("⚠️ تم فرض غرامة: " + fine + " NIS على الادمين: " + loanToReturn.getUser().getName());
    }

    public void payFineForUserInteractive(Scanner scanner) {
        System.out.println("\n=== دفع الغرامة  ===");
        System.out.print("أدخل بريد الادمين الإلكتروني: "); String email = scanner.nextLine().trim();
        User user = UserFileHandler.loadAllUsers().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (user == null) { System.out.println("❌ لم يتم العثور على الادمين."); return; }
        FineFileManager.loadFines(user);
        int fine = fineCalculator.calculateTotalFine(user);
        if (fine > 0) {
            System.out.println("لدى الادمين " + user.getName() + " غرامة مستحقة: " + fine + " شيكل.");
            System.out.print("هل تريد الدفع الآن؟ (y/n): ");
            String pay = scanner.nextLine().trim().toLowerCase();
            if (pay.equals("y")) {
                for (Fine f : user.getFines()) { if (!f.isPaid()) user.payFine(f); }
                FineFileManager.removePaidFines(user);
                System.out.print("أدخل رقم الحساب البنكي: ");
                String bank = scanner.nextLine().trim();
                System.out.println("✅ تم دفع جميع الغرامات للادمين: " + user.getName());
            } else System.out.println("تم إلغاء الدفع.");
        } else System.out.println("الادمين ليس لديه غرامات مستحقة.");
    }



    public User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }


    public boolean addAdmin(String email, String password, String id, String name) {
        return UserFileHandler.saveUser(email, password, "ADMIN", id, name);
    }

    public boolean deleteAdmin(String id) {
        User user = findUserById(id);
        if (user == null || !user.getRole().equalsIgnoreCase("ADMIN")) return false;
        boolean removed = UserFileHandler.removeUserById(id, loggedInRole.name());
        if (removed) users.remove(user);
        return removed;
    }

    public boolean unregisterUser(String userId) {
        User user = findUserById(userId);
        if (user == null || user.getRole().equalsIgnoreCase("SUPER_ADMIN") || user.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }
        if (borrowService.hasActiveLoans(user) || user.hasUnpaidFines()) return false;
        borrowService.unregisterUser(userId);
        boolean removed = UserFileHandler.removeUserById(userId, loggedInRole.name());
        if (removed) users.remove(user);
        return removed;
    }

    public int getUserTotalFine(String userId) {
        User user = findUserById(userId);
        if (user == null) return -1;
        FineFileManager.loadFines(user);
        return fineCalculator.calculateTotalFine(user);
    }

    public boolean payAllUserFines(User user) {
        if (user == null || user.getFines().isEmpty()) return false;
        for (Fine f : user.getFines()) {
            if (!f.isPaid()) user.payFine(f);
        }
        FineFileManager.removePaidFines(user);
        return true;
    }

    }

