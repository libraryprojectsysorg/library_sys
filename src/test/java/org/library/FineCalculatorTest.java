package org.library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;  // إضافة لـ any(Loan.class)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FineCalculatorTest {
    private FineCalculator calc;
    @Mock private BorrowService mockService;
    @Mock private User mockUser;
    @Mock private Loan mockBookLoan;
    @Mock private Loan mockCDLoan;
    @Mock private Book mockBook;
    @Mock private CD mockCD;
    @Mock private FineStrategy mockBookStrategy;
    @Mock private FineStrategy mockCDStrategy;

    @BeforeEach
    void setUp() {
        calc = new FineCalculator(mockService);

        List<Loan> mockLoans = new ArrayList<>();
        mockLoans.add(mockBookLoan);
        mockLoans.add(mockCDLoan);
        lenient().when(mockService.getLoans()).thenReturn(mockLoans);  // lenient لتجنب UnnecessaryStubbing

        lenient().when(mockBookLoan.getUser()).thenReturn(mockUser);
        lenient().when(mockCDLoan.getUser()).thenReturn(mockUser);
        lenient().when(mockService.isOverdue(mockBookLoan)).thenReturn(true);
        lenient().when(mockService.isOverdue(mockCDLoan)).thenReturn(true);
        lenient().when(mockBookLoan.getMedia()).thenReturn(mockBook);
        lenient().when(mockCDLoan.getMedia()).thenReturn(mockCD);
        lenient().when(mockBookLoan.getDueDate()).thenReturn(LocalDate.now().minusDays(5));
        lenient().when(mockCDLoan.getDueDate()).thenReturn(LocalDate.now().minusDays(3));

        lenient().when(mockBook.getFineStrategy()).thenReturn(mockBookStrategy);
        lenient().when(mockCD.getFineStrategy()).thenReturn(mockCDStrategy);
    }

    @Test
    void testCalculateTotalFineMixedMedia() {
        lenient().when(mockBookStrategy.calculateFine(5)).thenReturn(50);
        lenient().when(mockCDStrategy.calculateFine(3)).thenReturn(60);

        int total = calc.calculateTotalFine(mockUser);

        assertEquals(110, total);
        verify(mockService).getLoans();
    }

    @Test
    void testCalculateTotalFineNoOverdues() {
        lenient().when(mockService.isOverdue(any(Loan.class))).thenReturn(false);

        int total = calc.calculateTotalFine(mockUser);

        assertEquals(0, total);
        verify(mockService).getLoans();
    }
}