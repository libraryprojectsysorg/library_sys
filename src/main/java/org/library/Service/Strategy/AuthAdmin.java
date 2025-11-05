/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad


 */

package org.library.Service.Strategy;

import org.library.Domain.Book;
import org.library.Domain.User;
import org.library.Service.Strategy.*;
import org.library.Domain.CD;
import org.library.Service.Strategy.fines.FineCalculator;

import java.util.List;
import java.util.Scanner;
import io.github.cdimascio.dotenv.Dotenv;
public class AuthAdmin {

    private final List<User> users;
    public boolean isLoggedIn = false;
    private String loggedInEmail = null;

    private final BookService bookService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final FineCalculator fineCalculator;
    private static final Dotenv dotenv = Dotenv.load();
    private static final String ADMIN_EMAIL = dotenv.get("ADMIN_EMAIL");
    private static final String ADMIN_PASS = dotenv.get("ADMIN_PASS");


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

            System.out.println("1. Add Book");
            System.out.println("2. Delete Book");
            System.out.println("3. View All Books");
            System.out.println("4. Add CD");
            System.out.println("5. Delete CD");
            System.out.println("6. View All CDs");
            System.out.println("7. Send Overdue Reminders (US3.1)");
            System.out.println("8. Unregister User (US4.2)");
            System.out.println("9. Fine Summary (US5.3)");
            System.out.println("10. Logout (US1.2)");
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
                case 4: addCDInteractive(scanner); break;
                case 5: deleteCDInteractive(scanner); break;
                case 6: viewAllCDs(); break;

                case 7:
                    System.out.print("أدخل الإيميل الذي تريد إرسال رسالة له: ");
                    String targetEmail = scanner.nextLine().trim();

                    User targetUser = users.stream()
                            .filter(u -> u.getEmail().equalsIgnoreCase(targetEmail))
                            .findFirst()
                            .orElse(null);
                    if (targetUser == null) {
                        System.out.println("❌ المستخدم غير موجود.");
                        break;
                    }
                    int totalFine = fineCalculator.calculateTotalFine(targetUser);

                    if (totalFine == 0) {
                        System.out.println("✅ المستخدم ليس لديه أي غرامات مستحقة.");
                        break;
                    }
                    String messageContent = "يجب عليك دفع الغرامة المالية التي قدرها "
                            + totalFine + " شيكل بسبب تأخير إعادة الوسائط.";


                    RealEmailServer realEmailServer = new RealEmailServer();
                    EmailNotifier emailNotifier = new EmailNotifier(realEmailServer);
                    emailNotifier.notify(targetUser, messageContent);

                    System.out.println("✅ تم إرسال البريد الإلكتروني بنجاح إلى " + targetEmail);
                    break;

                case 8: // Unregister user
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

                case 9: // Fine summary
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

                case 10: logout(); System.out.println("Logged out."); break;

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
    public void addCDInteractive(Scanner scanner) {
        System.out.print("Enter CD code (ISBN/ID): ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Enter CD title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter CD author/artist: ");
        String author = scanner.nextLine().trim();

        try {
            boolean added = CDFileHandler.saveCD(new CD( title, author,isbn));
            if (added) System.out.println("✅ CD added successfully!");
            else System.out.println("⚠ A CD with this code already exists.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid CD details. Please try again.");
        }
    }

    public void deleteCDInteractive(Scanner scanner) {
        System.out.print("Enter CD code to delete: ");
        String code = scanner.nextLine().trim();

        boolean removed = CDFileHandler.removeCDByCode(code);
        if (removed) System.out.println("✅ CD deleted successfully!");
        else System.out.println("❌ No CD found with that code.");
    }

    public void viewAllCDs() {
        List<CD> allCDs = CDFileHandler.loadAllCDs();
        if (allCDs.isEmpty()) {
            System.out.println("📀 No CDs found.");
            return;
        }
        System.out.println("=== All CDs ===");
        for (CD cd : allCDs) {
            System.out.println("- Code: " + cd.getIsbn() + " | Title: " + cd.getTitle() + " | Author/Artist: " + cd.getAuthor());
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