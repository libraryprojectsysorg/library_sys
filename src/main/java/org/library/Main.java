package org.library;

import org.library.Domain.*;
import org.library.Service.Strategy.*;
import org.library.Service.Strategy.fines.FineCalculator;
import org.library.ui.AdminUI;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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

        RealEmailServer realEmailServer = new RealEmailServer();
        EmailNotifier emailNotifier = new EmailNotifier(realEmailServer);
        BorrowService borrowService = new BorrowService(emailNotifier);
        ReminderService reminderService = new ReminderService(List.of(emailNotifier), borrowService);
        FineCalculator fineCalculator = new FineCalculator(borrowService);
        BookCDService bookCDService = new BookCDService();
        AuthAdmin authAdmin = new AuthAdmin(borrowService, reminderService, fineCalculator, bookCDService);

        // ======== Admin UI ========
        AdminUI authUI = new AdminUI(
                authAdmin,
                scanner,
                bookCDService,
                borrowService,
                reminderService,
                fineCalculator
        );


        System.out.println("=== Library Management System ===");

        boolean exitProgram = false;

        while (!exitProgram) {
            String loggedInEmail = null;

            while (loggedInEmail == null) {
                System.out.print("\nهل لديك حساب مسجل بالفعل؟ (نعم/لا/نسيت كلمة السر/خروج): ");
                String response = scanner.nextLine().trim();

                if (response.equalsIgnoreCase("نعم")) {
                    System.out.print("أدخل بريدك الإلكتروني: ");
                    String email = scanner.nextLine().trim();
                    System.out.print("أدخل كلمة المرور: ");
                    String password = scanner.nextLine().trim();

                    if (authAdmin.login(email, password)) {
                        loggedInEmail = email;


                        authUI.updateRole(authAdmin);

                        System.out.println(authAdmin.getErrorMessage());
                    } else {
                        System.out.println("خطأ: البريد الإلكتروني أو كلمة المرور غير صحيحة.");
                    }

                } else if (response.equalsIgnoreCase("نسيت كلمة السر")) {
                    resetPasswordInteractive(scanner);

                } else if (response.equalsIgnoreCase("لا")) {
                    registerUserInteractive(scanner);

                } else if (response.equalsIgnoreCase("خروج")) {
                    exitProgram = true;
                    break;
                } else {
                    System.out.println("إجابة غير صالحة. يرجى إدخال (نعم/لا/نسيت كلمة السر/خروج).");
                }
            }

            if (exitProgram) break;

            if (authAdmin.isSuperAdmin()) {
                System.out.println("\nتم تسجيل الدخول كـ **مدير أعلى (SUPER ADMIN)**.");
                authUI.showAdminMenu();

                loggedInEmail = null;

            } else if (authAdmin.isLoggedInAdmin()) {
                System.out.println("\nتم تسجيل الدخول كـ **مدير عادي (ADMIN)**.");
                authUI.showAdminMenu();
                authUI.logout();
                loggedInEmail = null;

            } else if (authAdmin.isLoggedInUser()) {
                User user = findUserByEmail(loggedInEmail);
                if (user != null) {
                    System.out.println("\nتم تسجيل الدخول كـ **مستخدم عادي**.");
                    userMenu(scanner, borrowService, fineCalculator, bookCDService, user);
                    authUI.logout();
                    loggedInEmail = null;
                } else {
                    System.out.println("خطأ: لم يتم العثور على بيانات المستخدم.");
                }
            } else {
                System.out.println("❌ خطأ: لم يتم التعرف على الدور.");
            }

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

    private static void resetPasswordInteractive(Scanner scanner) {
        System.out.print("أدخل بريدك الإلكتروني: ");
        String email = scanner.nextLine().trim();

        User user = UserFileHandler.loadAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            System.out.println("❌ البريد الإلكتروني غير موجود.");
            return;
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        new EmailNotifier(new RealEmailServer()).notify(user,
                "رمز التحقق لإعادة تعيين كلمة المرور: " + otp);
        System.out.println("✅ تم إرسال رمز التحقق إلى بريدك الإلكتروني.");

        System.out.print("أدخل رمز التحقق: ");
        String enteredOtp = scanner.nextLine().trim();
        if (!enteredOtp.equals(otp)) {
            System.out.println("❌ الرمز غير صحيح.");
            return;
        }

        System.out.print("أدخل كلمة المرور الجديدة: ");
        String newPassword = scanner.nextLine().trim();
        System.out.print("أعد إدخال كلمة المرور الجديدة: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("❌ كلمات المرور غير متطابقة.");
            return;
        }

        user.setPassword(newPassword);
        UserFileHandler.updateUser(user);
        System.out.println("✅ تم تحديث كلمة المرور بنجاح.");
    }

    private static void userMenu(Scanner scanner, BorrowService borrowService, FineCalculator fineCalculator, BookCDService bookCDService, User user) {
        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1. Borrow Book");
            System.out.println("2. Return Book");
            System.out.println("3. Borrow CD");
            System.out.println("4. Return CD");
            System.out.println("5. Pay Fine");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("=== استعارة كتاب ===");
                    System.out.print("أدخل اسم الكتاب الذي تريد استعارته: ");
                    String title = scanner.nextLine().trim();

                    List<Book> matchingBooks = bookCDService.searchBooks(title);

                    if (matchingBooks.isEmpty()) {
                        System.out.println("خطأ: لم يتم العثور على كتاب بالعنوان المدخل.");
                        break;
                    }

                    Book bookToBorrow = matchingBooks.get(0);

                    try {
                        borrowService.borrowMedia(bookToBorrow, user);
                        System.out.println("✅ تم استعارة كتاب: " + bookToBorrow.getTitle() + " بنجاح!");
                    } catch (RuntimeException e) {
                        System.out.println("❌ فشل الاستعارة: " + e.getMessage());
                    }
                }

                case "2" -> {
                    System.out.println("=== إرجاع كتاب ===");
                    List<Loan> userLoans = borrowService.getLoans().stream()
                            .filter(loan -> loan.getUser().equals(user) && loan.getMedia() instanceof Book)
                            .toList();

                    if (userLoans.isEmpty()) {
                        System.out.println("❌ لا توجد إعارات كتب حالية للإرجاع.");
                        break;
                    }

                    for (Loan loan : userLoans) {
                        System.out.println("- Loan ID: " + loan.getLoanId() + " | Book: " + ((Book) loan.getMedia()).getTitle());
                    }

                    System.out.print("أدخل Loan ID للإرجاع: ");
                    String loanId = scanner.nextLine().trim();

                    boolean returned = borrowService.returnLoan(loanId);
                    System.out.println(returned ? "✅ تم الإرجاع بنجاح!" : "❌ رقم الإعارة غير صالح.");
                }

                case "3" -> {
                    System.out.println("=== استعارة CD ===");
                    System.out.print("أدخل اسم الـ CD الذي تريد استعارته: ");
                    String title = scanner.nextLine().trim();

                    List<CD> matchingCDs = bookCDService.searchCD(title);

                    if (matchingCDs.isEmpty()) {
                        System.out.println("❌ لم يتم العثور على CD بهذا الاسم.");
                        break;
                    }

                    CD cdToBorrow = matchingCDs.get(0);

                    try {
                        borrowService.borrowMedia(cdToBorrow, user);
                        System.out.println("✅ تم استعارة CD: " + cdToBorrow.getTitle() + " بنجاح!");
                    } catch (RuntimeException e) {
                        System.out.println("❌ فشل الاستعارة: " + e.getMessage());
                    }
                }

                case "4" -> {
                    System.out.println("=== إرجاع CD ===");
                    List<Loan> userLoans = borrowService.getLoans().stream()
                            .filter(loan -> loan.getUser().equals(user) && loan.getMedia() instanceof CD)
                            .toList();

                    if (userLoans.isEmpty()) {
                        System.out.println("❌ لا توجد إعارات CD حالية للإرجاع.");
                        break;
                    }

                    for (Loan loan : userLoans) {
                        System.out.println("- Loan ID: " + loan.getLoanId() + " | CD: " + ((CD) loan.getMedia()).getTitle());
                    }

                    System.out.print("أدخل Loan ID للإرجاع: ");
                    String loanId = scanner.nextLine().trim();

                    boolean returned = borrowService.returnLoan(loanId);
                    System.out.println(returned ? "✅ تم الإرجاع بنجاح!" : "❌ رقم الإعارة غير صالح.");
                }

                case "5" -> {
                    System.out.println("=== دفع الغرامة ===");

                    FineFileManager.loadFines(user);
                    int fine = fineCalculator.calculateTotalFine(user);

                    if (fine > 0) {
                        System.out.println("لديك " + fine + " شيكل كغرامة مستحقة.");
                        System.out.print("هل تريد الدفع الآن؟ (y/n): ");
                        String pay = scanner.nextLine().trim().toLowerCase();

                        if (pay.equals("y")) {
                            for (Fine f : user.getFines()) {
                                if (!f.isPaid()) user.payFine(f);
                            }

                            FineFileManager.removePaidFines(user);
                            System.out.print("أدخل رقم الحساب البنكي: ");
                            String bank = scanner.nextLine().trim();
                            System.out.println("✅ تم دفع جميع الغرامات بنجاح.");
                        } else {
                            System.out.println("تم إلغاء الدفع.");
                        }
                    } else {
                        System.out.println("لا توجد غرامات مستحقة.");
                    }
                }

                case "6" -> {
                    System.out.println("👋 وداعًا، " + user.getName() + "!");
                    return;
                }

                default -> System.out.println("❌ خيار غير صالح. حاول مرة أخرى.");
            }

        }
    }
}
