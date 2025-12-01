package org.library;

import org.junit.jupiter.api.*;
import org.library.Domain.CD;
import org.library.Service.Strategy.CDFileHandler;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDFileHandlerTest {

    private static final String TEST_FILE = "cds.txt"; // رح نستخدم نفس الملف الأصلي مع تنظيفه

    @BeforeEach
    void cleanup() throws Exception {
        // نمسح محتوى الملف قبل كل اختبار لضمان استقلالية الاختبارات
        Files.write(new File(TEST_FILE).toPath(), new byte[0]);
    }

    @AfterEach
    void cleanupAfter() throws Exception {
        // نمسح الملف بعد كل اختبار
        Files.write(new File(TEST_FILE).toPath(), new byte[0]);
    }

    @Test
    void testSaveCDSuccessfully() {
        CD cd = new CD("Title1", "Author1", "ISBN1");
        boolean saved = CDFileHandler.saveCD(cd);
        assertTrue(saved);

        List<CD> cds = CDFileHandler.loadAllCDs();
        assertEquals(1, cds.size());
        assertEquals("ISBN1", cds.get(0).getIsbn());
    }

    @Test
    void testSaveCDDuplicate() {
        CD cd1 = new CD("Title1", "Author1", "ISBN1");
        CD cd2 = new CD("Title2", "Author2", "ISBN1");
        CDFileHandler.saveCD(cd1);
        boolean saved = CDFileHandler.saveCD(cd2);
        assertFalse(saved);

        List<CD> cds = CDFileHandler.loadAllCDs();
        assertEquals(1, cds.size());
    }

    @Test
    void testRemoveCDByCode() {
        CD cd = new CD("Title1", "Author1", "ISBN1");
        CDFileHandler.saveCD(cd);

        boolean removed = CDFileHandler.removeCDByCode("ISBN1");
        assertTrue(removed);

        List<CD> cds = CDFileHandler.loadAllCDs();
        assertEquals(0, cds.size());
    }

    @Test
    void testRemoveNonExistingCD() {
        boolean removed = CDFileHandler.removeCDByCode("NON_EXISTING");
        assertFalse(removed);
    }

    @Test
    void testLoadAllCDsEmptyFile() {
        List<CD> cds = CDFileHandler.loadAllCDs();
        assertTrue(cds.isEmpty());
    }
}
