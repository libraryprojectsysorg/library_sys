package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.Domain.*;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.EmailNotifier;
import org.library.Service.Strategy.fines.FineStrategy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class BorrowTest {

    private BorrowService borrowService;
    private User user;
    private Book book;
    private CD cd;

    @BeforeEach
    void setup() {
        // يمكن تمرير null لأننا لن نرسل إيميلات في الاختبارات
        borrowService = new BorrowService(null);

        // إنشاء مستخدم
        user = new User("U001", "Weam Ahmad", "weam@example.com");

        // إنشاء وسائط
        book = new Book("Java Programming", "John Doe", "ISBN1234");
        cd = new CD("Top Hits", "DJ Mix", "CD5678");
    }

    @Test
    void testBorrowBookNoFines() {
        Loan loan = borrowService.borrowMedia(book, user);

        assertNotNull(loan);
        assertFalse(book.isAvailable());
        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getMedia());
    }

    @Test
    void testBorrowBookWithUnpaidFines() {
        // إضافة غرامة لمستخدم
        Fine fine = new Fine(10); // قيمة الغرامة 10
        user.addFine(fine);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                borrowService.borrowMedia(book, user)
        );

        assertEquals("Cannot borrow: overdue books or unpaid fines", exception.getMessage());
    }

    @Test
    void testReturnBookNoFine() {
        Loan loan = borrowService.borrowMedia(book, user);

        int fine = borrowService.returnMedia(loan);

        assertEquals(0, fine);
        assertTrue(book.isAvailable());
        assertFalse(user.hasUnpaidFines());
    }

    @Test
    void testReturnBookWithFine() {
        // إعداد ساعة وهمية لجعل القرض متأخر
        LocalDate pastDate = LocalDate.now().minusDays(book.getLoanDays() + 5);
        Clock mockClock = Clock.fixed(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        borrowService.setClock(mockClock);

        Loan loan = borrowService.borrowMedia(book, user);

        // إعادة الساعة للنظام الفعلي لإرجاع القرض
        borrowService.setClock(Clock.systemDefaultZone());

        int fine = borrowService.returnMedia(loan);

        assertTrue(fine > 0);
        assertTrue(user.hasUnpaidFines());
        assertTrue(book.isAvailable());
    }

    @Test
    void testBorrowCD() {
        Loan loan = borrowService.borrowMedia(cd, user);

        assertNotNull(loan);
        assertFalse(cd.isAvailable());
        assertEquals(cd, loan.getMedia());
    }
}
