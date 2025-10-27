package org.library.Service.Strategy;

import org.library.Domain.Book;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BookFileHandler {

    private static final String BOOKS_FILE = "books.txt";

    public static void saveBook(Book book) {
        try (FileWriter writer = new FileWriter(BOOKS_FILE, true);
             PrintWriter printWriter = new PrintWriter(writer)) {
            // التنسيق: Title,Author,ISBN
            String bookData = book.getTitle() + "," + book.getAuthor() + "," + book.getIsbn();
            printWriter.println(bookData);
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ الكتاب: " + e.getMessage());
        }
    }

    public static List<Book> loadAllBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    books.add(new Book(parts[0], parts[1], parts[2]));
                }
            }
        } catch (FileNotFoundException e) { }
        catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف الكتب: " + e.getMessage());
        }
        return books;
    }

    public static void rewriteAllBooks(List<Book> books) {
        try (FileWriter writer = new FileWriter(BOOKS_FILE, false);
             PrintWriter printWriter = new PrintWriter(writer)) {

            for (Book book : books) {
                String bookData = book.getTitle() + "," + book.getAuthor() + "," + book.getIsbn();
                printWriter.println(bookData);
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء إعادة كتابة ملف الكتب: " + e.getMessage());
        }
    }
}