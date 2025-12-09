package org.library;

import org.junit.jupiter.api.Test;
import org.library.Domain.CD;
import org.library.Domain.Media;
import org.library.Service.Strategy.fines.CDFineStrategy;
import org.library.Service.Strategy.fines.FineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class CDTest {

    @Test
    void shouldCreateCDWithValidDetails() {
        CD cd = new CD("Thriller", "Michael Jackson", "CD001");

        assertEquals("Thriller", cd.getTitle());
        assertEquals("Michael Jackson", cd.getAuthor());
        assertEquals("CD001", cd.getIsbn());
        assertTrue(cd.isAvailable());
    }

    @Test
    void shouldThrowException_WhenAnyFieldIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new CD(null, "Artist", "CD123"));

        assertThrows(IllegalArgumentException.class, () ->
                new CD("Album", null, "CD123"));

        assertThrows(IllegalArgumentException.class, () ->
                new CD("Album", "Artist", ""));

        assertThrows(IllegalArgumentException.class, () ->
                new CD("", "Artist", "CD123"));
    }

    @Test
    void getLoanDays_ShouldReturn7Days() {
        CD cd = new CD("Back in Black", "AC/DC", "CD888");

        assertEquals(7, cd.getLoanDays());
    }

    @Test
    void getFineStrategy_ShouldReturnCDFineStrategyInstance() {
        CD cd = new CD("Greatest Hits", "Queen", "CD999");

        FineStrategy strategy = cd.getFineStrategy();

        assertNotNull(strategy);
        assertTrue(strategy instanceof CDFineStrategy);
    }

    @Test
    void getDailyFineRate_ShouldReturn0_ForSuperAdmin() {
        CD cd = new CD("Test", "Test", "TEST");

        assertEquals(0, cd.getDailyFineRate("SUPER_ADMIN"));
        assertEquals(0, cd.getDailyFineRate("super_admin"));
    }

    @Test
    void getDailyFineRate_ShouldReturn10_ForAdminOrLibrarian() {
        CD cd = new CD("Test", "Test", "TEST");

        assertEquals(10, cd.getDailyFineRate("ADMIN"));
        assertEquals(10, cd.getDailyFineRate("admin"));
        assertEquals(10, cd.getDailyFineRate("LIBRARIAN"));
        assertEquals(10, cd.getDailyFineRate("Librarian"));
    }

    @Test
    void getDailyFineRate_ShouldReturn20_ForRegularUser() {
        CD cd = new CD("Test", "Test", "TEST");

        assertEquals(20, cd.getDailyFineRate("USER"));
        assertEquals(20, cd.getDailyFineRate("student"));
        assertEquals(20, cd.getDailyFineRate(""));        // فارغ
        assertEquals(20, cd.getDailyFineRate(null));       // null
    }

    @Test
    void shouldExtendMediaCorrectly() {
        CD cd = new CD("Album", "Artist", "CD123");

        assertTrue(cd instanceof Media);
    }
}