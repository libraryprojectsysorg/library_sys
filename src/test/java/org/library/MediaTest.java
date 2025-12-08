package org.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.library.Domain.Media;
import org.library.Service.Strategy.fines.FineStrategy;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    /**
     * هذا كلاس داخلي (Concrete Class) نستخدمه فقط لغايات الاختبار
     * لأن Media هو abstract ولا يمكن عمل new Media() مباشرة.
     */
    private static class MediaImpl extends Media {
        public MediaImpl(String title, String author, String isbn) {
            super(title, author, isbn);
        }

        @Override
        public int getLoanDays() {
            return 7; // قيمة تجريبية
        }

        @Override
        public FineStrategy getFineStrategy() {
            return null; // لا يهمنا هنا
        }

        @Override
        public int getDailyFineRate(String userRole) {
            return 10; // قيمة تجريبية
        }
    }

    @Test
    @DisplayName("Test creating valid Media object")
    void testConstructor_ValidInputs() {
        // Arrange
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";

        // Act
        Media media = new MediaImpl(title, author, isbn);

        // Assert
        assertEquals(title, media.getTitle());
        assertEquals(author, media.getAuthor());
        assertEquals(isbn, media.getIsbn());
        assertTrue(media.isAvailable(), "Media should be available by default");
    }

    @ParameterizedTest
    @DisplayName("Test constructor throws exception for invalid inputs")
    @CsvSource({
            ", Author, ISBN",       // Title is null
            "'', Author, ISBN",     // Title is empty
            "Title, , ISBN",        // Author is null
            "Title, '', ISBN",      // Author is empty
            "Title, Author, ",      // ISBN is null
            "Title, Author, ''"     // ISBN is empty
    })
    void testConstructor_InvalidInputs(String title, String author, String isbn) {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MediaImpl(title, author, isbn);
        });

        assertEquals("Invalid media details: title, author, or ISBN cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test availability setter and getter")
    void testAvailability() {
        // Arrange
        Media media = new MediaImpl("Title", "Author", "12345");

        // Act
        media.setAvailable(false);

        // Assert
        assertFalse(media.isAvailable(), "Media should be unavailable after setting it to false");

        // Act again
        media.setAvailable(true);

        // Assert again
        assertTrue(media.isAvailable(), "Media should be available after setting it to true");
    }
}