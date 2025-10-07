package Service;
import org.example.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class AuthBook {
    private List<Book> books = new ArrayList<>();

    public void addBook(String title, String author, String isbn) {
        if (books.stream().anyMatch(b -> b.getIsbn().equals(isbn))) {
            throw new IllegalArgumentException("Book with this ISBN already exists.");
        }
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);  // Searchable and available
    }

    public List<Book> searchBooks(String query) {
        String lowerQuery = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerQuery) ||
                        b.getAuthor().toLowerCase().contains(lowerQuery) ||
                        b.getIsbn().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}
