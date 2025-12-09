package org.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.domain.EmailMessage;
import org.library.domain.User;
import org.library.Service.strategy.EmailNotifier;
import org.library.Service.strategy.EmailServer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotifierTest {

    @Mock
    private EmailServer emailServer;

    @Test
    void shouldSendCorrectEmailMessage_WhenNotifyingUser() {
        // Arrange
        EmailNotifier notifier = new EmailNotifier(emailServer);
        User user = new User("U01", "Jane Doe", "jane@test.com", "USER");
        String messageBody = "Please return your book.";

        notifier.notify(user, messageBody);


        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailServer).send(messageCaptor.capture());

        EmailMessage sentEmail = messageCaptor.getValue();


        assertEquals("jane@test.com", sentEmail.getRecipientEmail());
        assertEquals("Overdue Books Reminder", sentEmail.getSubject());
        assertEquals("Please return your book.", sentEmail.getContent());
    }
}