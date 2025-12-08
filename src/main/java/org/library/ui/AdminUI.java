/*package org.library.ui;

import org.library.Domain.*;
import org.library.Service.Strategy.AuthAdmin;

import java.util.List;
import java.util.Scanner;

public class AdminUI {


    private final AuthAdmin authAdmin;
    private final Scanner scanner;

    public AdminUI(AuthAdmin authAdmin, Scanner scanner) {
        this.authAdmin = authAdmin;
        this.scanner = scanner;
    }

    public void showAdminMenu() {
        if (!authAdmin.isLoggedInAdmin()) {
            System.out.println("❌ هذه القائمة مخصصة للمدراء فقط.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Add Book");
            System.out.println("2. Delete Book");
            System.out.println("3. View All Books");
            System.out.println("4. Add CD");
            System.out.println("5. Delete CD");
            System.out.println("6. View All CDs");
            System.out.println("7. Logout");

            System.out.print("Choose option: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1 -> addBookInteractive();
                case 2 -> deleteBookInteractive();
                case 3 -> viewAllBooks();
                case 4 -> addCDInteractive();
                case 5 -> deleteCDInteractive();
                case 6 -> viewAllCDs();
                case 7 -> {
                    authAdmin.logout();
                    System.out.println("✅ Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void addBookInteractive() {
        System.out.print("Book title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        boolean added = authAdmin.addBook(title, author, isbn);
        System.out.println(added ? "✅ Book added!" : "⚠ Already exists.");
    }

    private void deleteBookInteractive() {
        System.out.print("ISBN to delete: ");
        String isbn = scanner.nextLine().trim();
        boolean removed = authAdmin.deleteBook(isbn);
        System.out.println(removed ? "✅ Book deleted." : "❌ Not found.");
    }

    private void viewAllBooks() {
        List<Book> books = authAdmin.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("📚 No books.");
            return;
        }
        books.forEach(b -> System.out.println("- " + b.getTitle() + " by " + b.getAuthor() + " (ISBN: " + b.getIsbn() + ")"));
    }

    private void addCDInteractive() {
        System.out.print("CD title: ");
        String title = scanner.nextLine().trim();
        System.out.print("CD author/artist: ");
        String author = scanner.nextLine().trim();
        System.out.print("CD code: ");
        String code = scanner.nextLine().trim();
        boolean added = authAdmin.addCD(title, author, code);
        System.out.println(added ? "✅ CD added!" : "⚠ Already exists.");
    }

    private void deleteCDInteractive() {
        System.out.print("CD code to delete: ");
        String code = scanner.nextLine().trim();
        boolean removed = authAdmin.deleteCD(code);
        System.out.println(removed ? "✅ CD deleted!" : "❌ Not found.");
    }

    private void viewAllCDs() {
        List<CD> cds = authAdmin.getAllCDs();
        if (cds.isEmpty()) {
            System.out.println("📀 No CDs.");
            return;
        }
        cds.forEach(cd -> System.out.println("- " + cd.getTitle() + " by " + cd.getAuthor() + " (Code: " + cd.getIsbn() + ")"));
    }


}*/