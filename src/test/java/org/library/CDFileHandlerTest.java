package org.library;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.library.Domain.CD;
import org.library.Service.Strategy.CDFileHandler;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDFileHandlerTest {

    @TempDir
    Path tempDir;

    private String testFilePath;

    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve("cds_test.txt").toString();
        CDFileHandler.setCdsFile(testFilePath);
    }

    @Test
    void saveCD_ShouldAddNewCD() {
        CD cd = new CD("Thriller", "Michael Jackson", "CD001");

        boolean result = CDFileHandler.saveCD(cd);

        assertTrue(result);
        List<CD> loaded = CDFileHandler.loadAllCDs();
        assertEquals(1, loaded.size());
        assertEquals("Thriller", loaded.get(0).getTitle());
    }

    @Test
    void saveCD_Duplicate_ShouldReturnFalse() {
        CD cd = new CD("Back in Black", "AC/DC", "CD002");
        CDFileHandler.saveCD(cd);

        boolean result = CDFileHandler.saveCD(cd);

        assertFalse(result);
        assertEquals(1, CDFileHandler.loadAllCDs().size());
    }

    @Test
    void removeCDByCode_ShouldRemoveSuccessfully() {
        CDFileHandler.saveCD(new CD("Test Album", "Artist", "DEL123"));

        boolean removed = CDFileHandler.removeCDByCode("DEL123");

        assertTrue(removed);
        assertTrue(CDFileHandler.loadAllCDs().isEmpty());
    }

    @Test
    void removeCDByCode_NonExisting_ShouldReturnFalse() {
        boolean removed = CDFileHandler.removeCDByCode("NONEXISTENT");
        assertFalse(removed);
    }

    @Test
    void loadAllCDs_FromEmptyFile_ShouldReturnEmptyList() {
        List<CD> cds = CDFileHandler.loadAllCDs();
        assertTrue(cds.isEmpty());
    }

    @Test
    void rewriteAllCDs_ShouldReplaceContent() {
        CDFileHandler.saveCD(new CD("Old CD", "Old Artist", "OLD001"));
        CDFileHandler.saveCD(new CD("Another", "Artist", "OLD002"));

        List<CD> newList = List.of(new CD("New Album", "New Artist", "NEW001"));
        CDFileHandler.rewriteAllCDs(newList);

        List<CD> loaded = CDFileHandler.loadAllCDs();
        assertEquals(1, loaded.size());
        assertEquals("New Album", loaded.get(0).getTitle());
    }
    @Test
    void saveCD_ShouldHandleIOException_Gracefully() {

        CDFileHandler.setCdsFile("/invalid/path/that/does/not/exist/cdstest.txt");

        CD cd = new CD("Test", "Artist", "CD123");

        boolean result = CDFileHandler.saveCD(cd);

        assertFalse(result);
    }
}