package org.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Service.strategy.RealEmailServer;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.library.domain.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealEmailServerTest {

    @Mock
    private Transport mockTransport;

    private RealEmailServer emailServer;

    @Test
    void testSend_ShouldSendEmailSuccessfully() {

        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).thenAnswer(invocation -> {

                return null;
            });

            emailServer = new RealEmailServer();  // يستخدم .env من test/resources
            EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");


            assertDoesNotThrow(() -> emailServer.send(message));


            mockedTransport.verify(() -> Transport.send(any()), times(1));
        }
    }

    @Test
    void testSend_ShouldThrowRuntimeException_OnAuthenticationFailure() {

        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(MimeMessage.class)))
                    .thenThrow(new MessagingException("535-5.7.8 Username and Password not accepted"));

            emailServer = new RealEmailServer();
            EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");


            RuntimeException exception = assertThrows(RuntimeException.class, () -> emailServer.send(message));
            assertTrue(exception.getMessage().contains("فشل إرسال الإيميل"));

            mockedTransport.verify(() -> Transport.send(any()), times(1));
        }
    }

    @Test
    void testSend_ShouldThrowRuntimeException_OnUnexpectedException() {
        // Mock لخطأ عام
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(MimeMessage.class)))
                    .thenThrow(new RuntimeException("Network error"));

            emailServer = new RealEmailServer();
            EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");

            RuntimeException exception = assertThrows(RuntimeException.class, () -> emailServer.send(message));
            assertTrue(exception.getMessage().contains("خطأ غير متوقع"));

            mockedTransport.verify(() -> Transport.send(any()), times(1));
        }
    }

    /*@Test
    void testConstructor_ShouldThrowIllegalStateException_WhenCredentialsMissing() {

        assertThrows(IllegalStateException.class, () -> {

            new RealEmailServer();
        });
    }*/
}