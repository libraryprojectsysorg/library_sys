/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */

package org.library.Service.strategy;

import org.library.domain.Book;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class BookFileHandler {
    private static final Logger LOGGER = Logger.getLogger(BookFileHandler.class.getName());
    private static String booksFile = "books.txt";


    public static void setBooksFile(String filePath) {
        booksFile = filePath;
    }

    public static void saveBook(Book book) {
        try (FileWriter writer = new FileWriter(booksFile, true);
             PrintWriter printWriter = new PrintWriter(writer)) {
            String bookData = book.getTitle() + "," + book.getAuthor() + "," + book.getIsbn();
            printWriter.println(bookData);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "❌ خطأ أثناء حفظ الكتاب: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "❌ خطأ أثناء قراءة ملف الكتب: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "❌ خطأ أثناء إعادة كتابة ملف الكتب: " + e.getMessage(), e);
        }
    }
}
