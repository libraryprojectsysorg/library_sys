package org.library.Service.Strategy;

import org.library.Domain.CD;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File handler for CDs (Sprint 5)
 * @author Weam Ahmad
 * @author Seba Abd Aljwwad
 * @version 1.0
 */
public class CDFileHandler {

    private static final String FILE_PATH = "cds.txt"; // ملف لتخزين CDs

    /**
     * Save a CD to file.
     * @param cd CD object
     * @return true if added, false if already exists
     */
    public static boolean saveCD(CD cd) {
        List<CD> cds = loadAllCDs();


        for (CD c : cds) {
            if (c.getIsbn().equalsIgnoreCase(cd.getIsbn())) {
                return false;
            }
        }


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**

     * @param code CD code
     * @return true if removed
     */
    public static boolean removeCDByCode(String code) {
        List<CD> cds = loadAllCDs();
        boolean removed = cds.removeIf(cd -> cd.getIsbn().equalsIgnoreCase(code));

        if (!removed) return false;


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (CD cd : cds) {
                writer.write(cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**

     * @return list of CDs
     */
    public static List<CD> loadAllCDs() {
        List<CD> cds = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return cds;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    String author = parts[2].trim();
                    String title = parts[1].trim();
                    String isbn = parts[0].trim();
                    cds.add(new CD(isbn, title, author));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cds;
    }
}
