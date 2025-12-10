package org.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.domain.User;
import org.library.Service.strategy.BorrowService;
import org.library.Service.strategy.Observer;
import org.library.Service.strategy.ReminderService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private BorrowService borrowService;

    @Mock
    private Observer mockObserver;

    private ReminderService reminderService;

    @BeforeEach
    void setUp() {

        reminderService = new ReminderService(List.of(mockObserver), borrowService);
    }

    @Test
    void shouldSendNotification_WhenUserHasOverdueLoans() {

        User user = new User("U01", "John Doe", "john@test.com", "USER");


        when(borrowService.getUsersWithOverdueLoans()).thenReturn(List.of(user));


        when(borrowService.countOverdueLoansForUser(user)).thenReturn(3);


        reminderService.sendReminders();


        String expectedMessage = "You have 3 overdue book(s).";


        verify(mockObserver, times(1)).notify(user, expectedMessage);

    }

    @Test
    void shouldNotSendNotification_WhenNoUsersHaveOverdueLoans() {

        when(borrowService.getUsersWithOverdueLoans()).thenReturn(Collections.emptyList());


        reminderService.sendReminders();


        verify(mockObserver, never()).notify(any(), any());
    }

    @Test
    void shouldSendToAllObservers_IfMultipleNotifiersExist() {

        Observer secondObserver = mock(Observer.class);


        reminderService = new ReminderService(List.of(mockObserver, secondObserver), borrowService);

        User user = new User("U02", "Jane", "jane@test.com", "USER");
        when(borrowService.getUsersWithOverdueLoans()).thenReturn(List.of(user));
        when(borrowService.countOverdueLoansForUser(user)).thenReturn(1);


        reminderService.sendReminders();


        verify(mockObserver).notify(eq(user), anyString());
        verify(secondObserver).notify(eq(user), anyString());
    }
}