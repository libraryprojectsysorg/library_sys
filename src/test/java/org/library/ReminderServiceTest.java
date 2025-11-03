package org.library;
import org.library.Service.Strategy.BorrowService;
import org.library.Service.Strategy.EmailNotifier;
import org.library.Service.Strategy.EmailServer;
import org.library.Domain.User;
import org.library.Service.Strategy.ReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.util.List;

/**
 * Tests for ReminderService (US3.1, Sprint 3).
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {
    @Mock
    private BorrowService mockBorrowService;
    @Mock
    private EmailServer mockEmailServer;
    @Mock
    private User mockUser;

    @Test
    void testSendReminders() {
        // Arrange
        EmailNotifier notifier = new EmailNotifier(mockEmailServer);
        ReminderService service = new ReminderService(List.of(notifier), mockBorrowService);
        when(mockBorrowService.getUsersWithOverdueLoans()).thenReturn(List.of(mockUser));
        when(mockBorrowService.countOverdueLoansForUser(mockUser)).thenReturn(2);
        when(mockUser.getEmail()).thenReturn("test@example.com");

        // Act
        service.sendReminders();

        // Assert: Mock records sent message
        verify(mockEmailServer).send(argThat(email ->
                email.getContent().equals("You have 2 overdue book(s).") &&
                        email.getRecipientEmail().equals("test@example.com")
        ));
    }

    // Optional: Test with time mock (integrate with Sprint 2 Clock)
    @Test
    void testSendRemindersWithMockTime() {
        // Set mock clock in borrowService to simulate overdues
        // ... (use Clock from Sprint 2)
        // Then call sendReminders and assert
    }
}