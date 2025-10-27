package org.library;

import org.library.Domain.Book;
import org.library.Domain.Fine;
import org.library.Domain.Loan;
import org.library.Domain.User;

import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ===== إعداد الخدمات (Dependency Injection) =====
        RealEmailServer realEmailServer = new RealEmailServer();
        EmailNotifier emailNotifier = new EmailNotifier(realEmailServer);
        BorrowService borrowService = new BorrowService(emailNotifier);
        ReminderService reminderService = new ReminderService(List.of(emailNotifier), borrowService);
        FineCalculator fineCalculator = new FineCalculator(borrowService);
        BookService bookService = new BookService();
        AuthAdmin authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookService);

        setupDemoData(borrowService, bookService);

        System.out.println("=== Library Management System ===");

        String loggedInEmail = null;

        while (loggedInEmail == null) {
            System.out.print("\nهل لديك حساب مسجل بالفعل؟ (نعم/لا/خروج): ");
            String response = scanner.nextLine().trim();

            if (response.equalsIgnoreCase("نعم")) {
                System.out.print("أدخل بريدك الإلكتروني: ");
                String email = scanner.nextLine().trim();
                System.out.print("أدخل كلمة المرور: ");
                String password = scanner.nextLine().trim();

                if (authAdmin.login(email, password)) {
                    loggedInEmail = email;
                    System.out.println(authAdmin.getErrorMessage());
                } else {
                    System.out.println("❌ خطأ: البريد الإلكتروني أو كلمة المرور غير صحيحة.");
                }

            } else if (response.equalsIgnoreCase("لا")) {
                registerUserInteractive(scanner);

            } else if (response.equalsIgnoreCase("خروج")) {
                System.out.println("Exiting...");
                scanner.close();
                return;
            } else {
                System.out.println("إجابة غير صالحة. يرجى إدخال (نعم/لا/خروج).");
            }
        }

        if (authAdmin.isLoggedInAdmin()) {
            System.out.println("\n🌟 تم تسجيل الدخول كـ **مدير**.");
            authAdmin.showAdminMenu();
        } else {
            User user = findUserByEmail(loggedInEmail);
            if (user != null) {
                System.out.println("\n👤 تم تسجيل الدخول كـ **مستخدم عادي**.");
                userMenu(scanner, borrowService, fineCalculator, bookService, user);
            } else {
                System.out.println("❌ خطأ: لم يتم العثور على بيانات المستخدم.");
            }
        }

        scanner.close();
        System.out.println("System exited.");
    }

    private static void registerUserInteractive(Scanner scanner) {
        System.out.println("\n=== تسجيل مستخدم جديد ===");
        System.out.print("أدخل اسمك الكامل: ");
        String name = scanner.nextLine().trim();
        System.out.print("أدخل بريدك الإلكتروني (لتسجيل الدخول لاحقاً): ");
        String email = scanner.nextLine().trim();
        System.out.print("أنشئ كلمة مرور: ");
        String password = scanner.nextLine().trim();

        UserFileHandler.saveUser(email, password, "USER", null, name);
        System.out.println("✅ تم تسجيل حسابك بنجاح. يمكنك الآن تسجيل الدخول.");
    }

    private static User findUserByEmail(String email) {
        List<User> allUsers = UserFileHandler.loadAllUsers();
        return allUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    /** قائمة المستخدم العادي (User). */
    private static void userMenu(Scanner scanner, BorrowService borrowService, FineCalculator fineCalculator, BookService bookService, User user) {
        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1. Borrow Item");
            System.out.println("2. Return Item");
            System.out.println("3. Pay Fine");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" : {
                    System.out.println("=== استعارة كتاب ===");
                    System.out.print("أدخل ISBN (رقم دولي معياري للكتاب) للبحث: ");
                    String isbn = scanner.nextLine().trim();

                    List<Book> matchingBooks = bookService.searchBooks(isbn);

                    if (matchingBooks.isEmpty()) {
                        System.out.println("❌ خطأ: لم يتم العثور على كتاب بالـ ISBN المدخل.");
                        break;
                    }

                    Book bookToBorrow = matchingBooks.get(0);

                    try {
                        borrowService.borrowMedia(bookToBorrow, user);

                        System.out.println("✅ تم استعارة كتاب: " + bookToBorrow.getTitle() + " بنجاح!");
                    } catch (RuntimeException e) {
                        System.out.println("❌ فشل الاستعارة: " + e.getMessage());
                    }
                    break;
                }
                case "2" : {
                    System.out.print("Enter loan ID to return: ");
                    String loanId = scanner.nextLine();
                    boolean returned = borrowService.returnLoan(loanId);
                    if (returned)
                        System.out.println("✅ تم إرجاع الكتاب بنجاح!");
                    else
                        System.out.println("❌ خطأ: رقم الإعارة غير صالح أو تم إرجاعه بالفعل.");
                    break;
                }
                case "3" : {
                    int fine = fineCalculator.calculateTotalFine(user);
                    if (fine > 0) {
                        System.out.println("You have " + fine + " NIS fine.");
                        System.out.print("Pay now? (y/n): ");
                        String pay = scanner.nextLine().toLowerCase();
                        if (pay.equals("y")) {
                            for (Fine f : user.getFines()) {
                                if (!f.isPaid()) {
                                    user.payFine(f);
                                }
                            }
                            System.out.println("✅ تم دفع جميع الغرامات بنجاح.");
                        } else {
                            System.out.println("Payment canceled.");
                        }
                    } else {
                        System.out.println("✅ لا توجد غرامات مستحقة.");
                    }
                    break;
                }
                case "4" : {
                    System.out.println("Goodbye, " + user.getName() + "!");
                    return;
                }
                default :  System.out.println("Invalid option. Try again.");
            }
        }
    }

    /** بيانات تجريبية لتجربة النظام. */
    private static void setupDemoData(BorrowService borrowService, BookService bookService) {

        String demoEmail = "demo@example.com";
        // 1. ضمان وجود المستخدم التجريبي
        if (UserFileHandler.getUserByCredentials(demoEmail, "pass123") == null) {
            UserFileHandler.saveUser(demoEmail, "pass123", "USER", "U001", "Demo User");
        }

        // 2. ضمان وجود الكتاب التجريبي
        try {
            bookService.addBook("Demo Overdue Book", "Test Author", "999888777");
        } catch (IllegalArgumentException e) { }

        // 3. إنشاء قرض متأخر
        User demoUser = findUserByEmail(demoEmail);
        Book demoBook = new Book("Demo Overdue Book", "Test Author", "999888777");

        LocalDate oldBorrowDate = LocalDate.now().minusDays(30);

        // منع إضافة القرض التجريبي أكثر من مرة
        if (borrowService.getLoans().stream().noneMatch(loan -> loan.getMedia().equals("DEMO_LOAN"))) {
            if (demoUser != null) {
                Loan demoLoan = new Loan("DEMO_LOAN", demoBook, demoUser, oldBorrowDate, oldBorrowDate.plusDays(28));
                borrowService.addLoan(demoLoan);
            }
        }

        System.out.println("Demo data loaded: 1 overdue book for testing reminders.");
    }
}