/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */

package org.library.Service.Strategy;

import org.library.Domain.Book;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BookFileHandler {

    private static String booksFile = "books.txt"; // غير final


    public static void setBooksFile(String filePath) {
        booksFile = filePath;
    }

    public static void saveBook(Book book) {
        try (FileWriter writer = new FileWriter(booksFile, true);
             PrintWriter printWriter = new PrintWriter(writer)) {
            String bookData = book.getTitle() + "," + book.getAuthor() + "," + book.getIsbn();
            printWriter.println(bookData);
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ الكتاب: " + e.getMessage());
        }
    }

    public static List<Book> loadAllBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(booksFile))) {
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
        try (FileWriter writer = new FileWriter(booksFile, false);
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
