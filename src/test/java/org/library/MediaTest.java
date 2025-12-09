package org.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.library.domain.Media;
import org.library.Service.Strategy.fines.FineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {


    private static class MediaImpl extends Media {
        public MediaImpl(String title, String author, String isbn) {
            super(title, author, isbn);
        }

        @Override
        public int getLoanDays() {
            return 7;
        }

        @Override
        public FineStrategy getFineStrategy() {
            return null;
        }

        @Override
        public int getDailyFineRate(String userRole) {
            return 10;
        }
    }

    @Test
    @DisplayName("Test creating valid Media object")
    void testConstructor_ValidInputs() {

        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";

        Media media = new MediaImpl(title, author, isbn);

        assertEquals(title, media.getTitle());
        assertEquals(author, media.getAuthor());
        assertEquals(isbn, media.getIsbn());
        assertTrue(media.isAvailable(), "Media should be available by default");
    }

    @ParameterizedTest
    @DisplayName("Test constructor throws exception for invalid inputs")
    @CsvSource({
            ", Author, ISBN",
            "'', Author, ISBN",
            "Title, , ISBN",
            "Title, '', ISBN",
            "Title, Author, ",
            "Title, Author, ''"
    })
    void testConstructor_InvalidInputs(String title, String author, String isbn) {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MediaImpl(title, author, isbn);
        });

        assertEquals("Invalid media details: title, author, or ISBN cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test availability setter and getter")
    void testAvailability() {

        Media media = new MediaImpl("Title", "Author", "12345");


        media.setAvailable(false);


        assertFalse(media.isAvailable(), "Media should be unavailable after setting it to false");


        media.setAvailable(true);


        assertTrue(media.isAvailable(), "Media should be available after setting it to true");
    }
}