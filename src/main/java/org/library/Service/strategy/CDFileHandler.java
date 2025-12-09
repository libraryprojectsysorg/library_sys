package org.library.Service.strategy;

import org.library.domain.CD;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CDFileHandler {

    private static final Logger logger = Logger.getLogger(CDFileHandler.class.getName());

    private static String cdsFile = "cds.txt";

    private CDFileHandler() {}

    public static void setCdsFile(String filePath) {
        cdsFile = filePath;
    }

    public static boolean saveCD(CD cd) {
        List<CD> cds = loadAllCDs();
        String cdTitle = cd.getTitle();

        for (CD c : cds) {
            if (c.getIsbn().equals(cd.getIsbn())) {
                return false;
            }
        }

        try (FileWriter writer = new FileWriter(cdsFile, true);
             PrintWriter printWriter = new PrintWriter(writer)) {
            String cdData = cdTitle + "," + cd.getAuthor() + "," + cd.getIsbn();
            printWriter.println(cdData);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "خطأ أثناء حفظ CD: " + cdTitle, e);
            return false;
        }
    }

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
            logger.log(Level.SEVERE, "خطأ أثناء إعادة كتابة ملف CDs: " + code, e);
            return false;
        }
    }

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
            logger.log(Level.SEVERE, "خطأ أثناء قراءة ملف CDs", e);
        }

        return cds;
    }

    public static void rewriteAllCDs(List<CD> cds) {
        try (FileWriter writer = new FileWriter(cdsFile, false);
             PrintWriter printWriter = new PrintWriter(writer)) {
            for (CD cd : cds) {
                String cdData = cd.getTitle() + "," + cd.getAuthor() + "," + cd.getIsbn();
                printWriter.println(cdData);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "خطأ أثناء إعادة كتابة ملف CDs", e);
        }
    }
}
