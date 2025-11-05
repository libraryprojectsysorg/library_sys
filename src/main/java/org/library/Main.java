package org.library;

import org.library.Domain.*;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        // ===== إعداد الخدمات =====
        RealEmailServer realEmailServer = new RealEmailServer();
        EmailNotifier emailNotifier = new EmailNotifier(realEmailServer);
        BorrowService borrowService = new BorrowService(emailNotifier);
        ReminderService reminderService = new ReminderService(List.of(emailNotifier), borrowService);
        FineCalculator fineCalculator = new FineCalculator(borrowService);
        BookService bookService = new BookService();
        AuthAdmin authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookService);

        setupDemoData(borrowService, bookService);

        System.out.println("=== Library Management System ===");

        boolean exitProgram = false;

        while (!exitProgram) {  // ← الحلقة الرئيسية
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
                        System.out.println("خطأ: البريد الإلكتروني أو كلمة المرور غير صحيحة.");
                    }

                } else if (response.equalsIgnoreCase("لا")) {
                    registerUserInteractive(scanner);

                } else if (response.equalsIgnoreCase("خروج")) {
                    exitProgram = true;
                    break;
                } else {
                    System.out.println("إجابة غير صالحة. يرجى إدخال (نعم/لا/خروج).");
                }
            }

            if (exitProgram) break;

            if (authAdmin.isSuperAdmin()) {
                System.out.println("\nتم تسجيل الدخول كـ **مدير أعلى (SUPER ADMIN)**.");
                authAdmin.showAdminMenu(scanner); // تشمل كل الخيارات
            } else if (authAdmin.isLoggedInAdmin()) {
                System.out.println("\nتم تسجيل الدخول كـ **مدير عادي (ADMIN)**.");
                authAdmin.showAdminMenu(scanner); // تشمل خيارات المدير العادي فقط
            } else if (authAdmin.isLoggedInUser()) {
                User user = findUserByEmail(loggedInEmail);
                if (user != null) {
                    System.out.println("\nتم تسجيل الدخول كـ **مستخدم عادي**.");
                    userMenu(scanner, borrowService, fineCalculator, bookService, user);
                } else {
                    System.out.println("خطأ: لم يتم العثور على بيانات المستخدم.");
                }
            } else {
                System.out.println("❌ خطأ: لم يتم التعرف على الدور.");
            }



            // بعد انتهاء أي جلسة، يعود البرنامج للحلقة الرئيسية لتسجيل الدخول مرة أخرى
            System.out.println("\n=== العودة إلى شاشة تسجيل الدخول ===");
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
        System.out.println("تم تسجيل حسابك بنجاح. يمكنك الآن تسجيل الدخول.");
    }

    private static User findUserByEmail(String email) {
        List<User> allUsers = UserFileHandler.loadAllUsers();
        return allUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

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
                case "1" -> {
                    System.out.println("=== استعارة كتاب ===");
                    System.out.print("أدخل اسم الكتاب الذي تريد استعارته: ");
                    String title = scanner.nextLine().trim();

                    List<Book> matchingBooks = bookService.searchBooks(title);

                    if (matchingBooks.isEmpty()) {
                        System.out.println("خطأ: لم يتم العثور على كتاب بالعنوان المدخل.");
                        break;
                    }

                    Book bookToBorrow = matchingBooks.get(0);

                    try {
                        borrowService.borrowMedia(bookToBorrow, user);
                        System.out.println("تم استعارة كتاب: " + bookToBorrow.getTitle() + " بنجاح!");
                    } catch (RuntimeException e) {
                        System.out.println("فشل الاستعارة: " + e.getMessage());
                    }
                }
                case "2" -> {

                    List<Loan> userLoans = borrowService.getLoans().stream()
                            .filter(loan -> loan.getUser().equals(user))
                            .toList();

                    if (userLoans.isEmpty()) {
                        System.out.println("❌ لا توجد إعارات حالية للإرجاع.");
                        break;
                    }

                    System.out.println("📋 قائمة الإعارات الحالية:");
                    for (Loan loan : userLoans) {
                        String mediaType = loan.getMedia() instanceof Book ? "Book" : "CD";
                        String title = loan.getMedia() instanceof Book ? ((Book)loan.getMedia()).getTitle()
                                : ((CD)loan.getMedia()).getTitle();
                        System.out.println("- Loan ID: " + loan.getLoanId() + " | " + mediaType + ": " + title);
                    }

                    // 2️⃣ طلب Loan ID من المستخدم
                    System.out.print("أدخل Loan ID للإرجاع: ");
                    String loanId = scanner.nextLine().trim();

                    boolean returned = borrowService.returnLoan(loanId);
                    if (returned)
                        System.out.println("✅ تم إرجاع العنصر بنجاح!");
                    else
                        System.out.println("❌ خطأ: رقم الإعارة غير صالح أو تم إرجاعه بالفعل.");
                    break;
                }
                case "3" -> {

                    FineFileManager.loadFines(user);

                    int fine = fineCalculator.calculateTotalFine(user);
                    if (fine > 0) {
                        System.out.println("لديك " + fine + " شيكل كغرامة مستحقة.");
                        System.out.print("هل تريد الدفع الآن؟ (y/n): ");
                        String pay = scanner.nextLine().trim().toLowerCase();

                        if (pay.equals("y")) {

                            for (Fine f : user.getFines()) {
                                if (!f.isPaid()) {
                                    user.payFine(f);
                                }
                            }


                            FineFileManager.removePaidFines(user);
                            System.out.println("ادخل رقم حساب البطاقة البنكية: ");
                            String bank = scanner.nextLine().trim().toLowerCase();
                            System.out.println("✅ تم دفع جميع الغرامات ");
                        } else {
                            System.out.println("تم إلغاء الدفع.");
                        }
                    } else {
                        System.out.println("لا توجد غرامات مستحقة.");
                    }
                }

                case "4" -> {
                    System.out.println("Goodbye, " + user.getName() + "!");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void setupDemoData(BorrowService borrowService, BookService bookService) {
        String demoEmail = "s12217424@stu.najah.edu";
        if (UserFileHandler.getUserByCredentials(demoEmail, "er1234") == null) {
            UserFileHandler.saveUser(demoEmail, "er1234", "USER", "U1A2F7", " صبا عبد  الجواد");
        }

        try {
            bookService.addBook("Demo Overdue Book", "Test Author", "999888777");
        } catch (IllegalArgumentException e) { }

        User demoUser = findUserByEmail(demoEmail);
        Book demoBook = new Book("Demo Overdue Book", "Test Author", "999888777");

        LocalDate oldBorrowDate = LocalDate.now().minusDays(30);

        if (borrowService.getLoans().stream().noneMatch(loan -> loan.getLoanId().equals("DEMO_LOAN"))) {
            if (demoUser != null) {
                Loan demoLoan = new Loan("DEMO_LOAN", demoBook, demoUser, oldBorrowDate, oldBorrowDate.plusDays(28));
                borrowService.addLoan(demoLoan);
            }
        }

        System.out.println("Demo data loaded: 1 overdue book for testing reminders.");
    }
}