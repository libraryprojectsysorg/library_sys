/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad


 */

package org.library.Service.Strategy;

import org.library.Domain.Book;
import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.util.List;
import java.util.Scanner;

public class AuthAdmin {

    private final List<User> users;
    public boolean isLoggedIn = false;
    private String loggedInEmail = null;

    private final BookService bookService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;

    private static final String ADMIN_EMAIL = "s12217663@stu.najah.edu";
    private static final String ADMIN_PASS = "ws1234";

    public AuthAdmin(BorrowService borrowService, ReminderService reminderService, FineCalculator fineCalculator, BookService bookService) {

        // 1. ضمان وجود الأدمن في الملف
        if (UserFileHandler.getUserByCredentials(ADMIN_EMAIL, ADMIN_PASS) == null) {
            UserFileHandler.saveUser(ADMIN_EMAIL, ADMIN_PASS, "ADMIN", "A001", "Library Admin");
        }

        // 2. تحميل المستخدمين العاديين من الملف
        this.users = UserFileHandler.loadAllUsers();

        this.borrowService = borrowService;
        this.reminderService = reminderService;
        this.fineCalculator = fineCalculator;
        this.bookService = bookService;
    }

    public boolean login(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            return false;
        }

        String foundEmail = UserFileHandler.getUserByCredentials(email, password);

        if (foundEmail != null) {
            isLoggedIn = true;
            loggedInEmail = foundEmail;
            return true;
        }
        return false;
    }

    public boolean isLoggedInAdmin() {
        return isLoggedIn && UserFileHandler.isAdmin(loggedInEmail);
    }

    public String getErrorMessage() {
        if (!isLoggedIn) {
            return "Invalid credentials - please try again.";
        }
        return "Login successful";
    }

    public void logout() {
        isLoggedIn = false;
        loggedInEmail = null;
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
            System.out.println("4. Send Overdue Reminders (US3.1)");
            System.out.println("5. Unregister User (US4.2)");
            System.out.println("6. Fine Summary (US5.3)");
            System.out.println("7. Logout (US1.2)");
            System.out.print("Choose option: ");

            int choice;
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("No input. Exiting menu.");
                    break;
                }
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: addBookInteractive(scanner); break;
                case 2: deleteBookInteractive(scanner); break;
                case 3: viewAllBooks(); break;
                case 4: reminderService.sendReminders(); break;

                case 5: // Unregister user
                    System.out.print("Enter user ID to unregister: ");
                    if (!scanner.hasNextLine()) break;
                    String userId = scanner.nextLine().trim();
                    User user = findUserById(userId);

                    if (user == null) {
                        System.out.println("❌ المستخدم ذي الـ ID " + userId + " غير موجود.");
                        break;
                    }

                    if (borrowService.hasActiveLoans(user) || user.hasUnpaidFines()) {
                        System.out.println("⚠️ لا يمكن إلغاء التسجيل: للمستخدم قروض نشطة أو غرامات غير مدفوعة.");
                        break;
                    }

                    borrowService.unregisterUser(userId);
                    this.users.remove(user);
                    boolean removedFromFile = UserFileHandler.removeUserById(userId);
                    System.out.println(removedFromFile ?
                            "✅ تم إلغاء تسجيل المستخدم بنجاح!" :
                            "❌ خطأ: فشل في حذف المستخدم من الملف.");
                    break;

                case 6: // Fine summary
                    System.out.print("Enter user ID for fine summary: ");
                    if (!scanner.hasNextLine()) break;
                    userId = scanner.nextLine().trim();
                    user = findUserById(userId);
                    if (user == null) {
                        System.out.println("User not found.");
                        break;
                    }
                    int total = fineCalculator.calculateTotalFine(user);
                    System.out.println("إجمالي الغرامات المستحقة: " + total + " NIS.");
                    break;

                case 7: logout(); System.out.println("Logged out."); break;

                default: System.out.println("Invalid option. Try again."); break;
            }
        }
    }


    private User findUserById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addBookInteractive(Scanner scanner) {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter author name: ");
        String author = scanner.nextLine().trim();
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        try {
            boolean added = bookService.addBook(title, author, isbn);
            if (added) System.out.println("✅ Book added successfully!");
            else System.out.println("⚠️ A book with this ISBN already exists.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid book details. Please try again.");
        }
    }

    public void deleteBookInteractive(Scanner scanner) {
        System.out.print("Enter ISBN of the book to delete: ");
        String isbn = scanner.nextLine().trim();

        List<Book> matches = bookService.searchBooks(isbn);
        if (matches.isEmpty()) {
            System.out.println("⚠️ No book found with that ISBN.");
            return;
        }

        Book book = matches.get(0);
        boolean removed = bookService.removeByIsbn(isbn);
        if (removed) System.out.println("✅ Book deleted: " + book.getTitle());
        else System.out.println("❌ Failed to delete book.");
    }

    public void viewAllBooks() {
        List<Book> all = bookService.searchBooks("");
        if (all.isEmpty()) {
            System.out.println("📚 No books found.");
            return;
        }
        System.out.println("=== All Books ===");
        all.forEach(b -> System.out.println("- " + b.getTitle() + " by " + b.getAuthor() + " (ISBN: " + b.getIsbn() + ")"));
    }
}