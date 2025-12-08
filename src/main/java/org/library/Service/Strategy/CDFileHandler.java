package org.library.Service.Strategy;
import org.library.Domain.CD;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Weam Ahmad
 * @author Seba Abd Aljwwad
 * @version 1.0
 */
public class CDFileHandler {
    private static String cdsFile = "cds.txt";
    public static void setCdsFile(String filePath) {
        cdsFile = filePath;
    }
    /**
     * Save a CD to file.
     * @param cd CD object
     * @return true if added, false if already exists
     */
    public static boolean saveCD(CD cd) {
        List<CD> cds = loadAllCDs();


        for (CD c : cds) {
            if (c.getIsbn().equals(cd.getIsbn())) {
                return false;
            }
        }

        try (FileWriter writer = new FileWriter(cdsFile, true);
             PrintWriter printWriter = new PrintWriter(writer)) {
            String cdData = cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn();
            printWriter.println(cdData);
            return true;
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ CD: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove a CD by its code (ISBN)
     * @param code CD code
     * @return true if removed
     */
    public static boolean removeCDByCode(String code) {
        List<CD> cds = loadAllCDs();
        boolean removed = cds.removeIf(cd -> cd.getIsbn().equals(code));
        if (!removed) return false;
        try (FileWriter writer = new FileWriter(cdsFile, false);
             PrintWriter printWriter = new PrintWriter(writer)) {
            for (CD cd : cds) {
                String cdData = cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn();
                printWriter.println(cdData);
            }
            return true;
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء إعادة كتابة ملف CDs: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load all CDs from file
     * @return list of CDs
     */
    public static List<CD> loadAllCDs() {
        List<CD> cds = new ArrayList<>();
        File file = new File(cdsFile);
        if (!file.exists()) return cds;
       try (BufferedReader reader = new BufferedReader(new FileReader(cdsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    cds.add(new CD(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف CDs: " + e.getMessage());
        }

        return cds;
    }

    /**
     * Rewrite all CDs to file
     * @param cds list of CDs
     */
    public static void rewriteAllCDs(List<CD> cds) {
        try (FileWriter writer = new FileWriter(cdsFile, false);
             PrintWriter printWriter = new PrintWriter(writer)) {
            for (CD cd : cds) {
                String cdData = cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn();
                printWriter.println(cdData);
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء إعادة كتابة ملف CDs: " + e.getMessage());
        }
    }
}
