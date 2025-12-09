package org.library.Service.Strategy;

import org.library.domain.*;
import org.library.Service.Strategy.fines.FineCalculator;

import java.util.ArrayList;
import java.util.List;

public class AuthAdmin {

    @SuppressWarnings("unused")
    private String loggedInEmail;

    private final List<User> users;
    private boolean isLoggedIn = false;


    private Role loggedInRole = null;

    private final BookCDService bookCDService;
    private final BorrowService borrowService;
    @SuppressWarnings("unused")
    private ReminderService reminderService;

    private final FineCalculator fineCalculator;

    private static final String SUPER_ADMIN_EMAIL = System.getenv("ADMIN_EMAIL") != null ? System.getenv("ADMIN_EMAIL") : "default_super@library.com";
    private static final String SUPER_ADMIN_PASS = System.getenv("ADMIN_PASS") != null ? System.getenv("ADMIN_PASS") : "default_superpass123";

    public enum Role {SUPER_ADMIN, ADMIN, USER}

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

    public void logout() {
        isLoggedIn = false;
        loggedInEmail = null;
        loggedInRole = null;
    }

// ======= User/Admin Operations =======

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
        if (user == null || user.getRole().equalsIgnoreCase("SUPER_ADMIN") || user.getRole().equalsIgnoreCase("ADMIN")) return false;
        if (borrowService.hasActiveLoans(user) || user.hasUnpaidFines()) return false;
        borrowService.unregisterUser(userId);
        boolean removed = UserFileHandler.removeUserById(userId, loggedInRole.name());
        if (removed) users.remove(user);
        return removed;
    }

    public User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
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



    public boolean addBook(String title, String author, String isbn) {
        return bookCDService.addBook(title, author, isbn);
    }

    public boolean deleteBook(String isbn) {
        return bookCDService.removeByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        return bookCDService.searchBooks("");
    }

    public boolean addCD(String title, String author, String code) {
        return bookCDService.addCD(title, author, code);
    }

    public boolean deleteCD(String code) {
        return bookCDService.removeCDByCode(code);
    }

    public List<CD> getAllCDs() {
        return bookCDService.searchCD("");
    }

// ======= Borrow & Return =======

    public boolean borrowMedia(Media media, User user) {
        try {
            borrowService.borrowMedia(media, user);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public int returnMedia(Loan loan) {
        return borrowService.returnMedia(loan.getLoanId());
    }

    public String getErrorMessage() {
        return !isLoggedIn ? "Invalid credentials - please try again." : "Login successful";
    }


}