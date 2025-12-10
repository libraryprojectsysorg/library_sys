package org.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.Service.strategy.RealEmailServer;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.library.domain.EmailMessage;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealEmailServerTest {

    @Test
    void testConstructor_ShouldThrowIllegalStateException_WhenCredentialsMissing() {
        // Mock Dotenv لإرجاع null
        try (MockedStatic<Dotenv> mockedDotenv = mockStatic(Dotenv.class)) {
            Dotenv mockEnv = mock(Dotenv.class);
            when(mockEnv.get("ADMIN_EMAIL")).thenReturn(null);
            when(mockEnv.get("ADMIN_PASS")).thenReturn("pass");
            mockedDotenv.when(Dotenv::load).thenReturn(mockEnv);

            // توقع رمي IllegalStateException
            assertThrows(IllegalStateException.class, RealEmailServer::new);
        }
    }

    @Test
    void testSend_ShouldSendEmailSuccessfully() {
        // Mock Dotenv لcredentials صالحة
        try (MockedStatic<Dotenv> mockedDotenv = mockStatic(Dotenv.class)) {
            Dotenv mockEnv = mock(Dotenv.class);
            when(mockEnv.get("ADMIN_EMAIL")).thenReturn("test@gmail.com");
            when(mockEnv.get("ADMIN_PASS")).thenReturn("testpass");
            mockedDotenv.when(Dotenv::load).thenReturn(mockEnv);

            // Mock Transport لنجاح
            try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
                mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).thenAnswer(invocation -> null);

                RealEmailServer server = new RealEmailServer();
                EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");

                // تنفيذ
                assertDoesNotThrow(() -> server.send(message));

                // تحقق: Transport تم استدعاء مرة واحدة
                mockedTransport.verify(() -> Transport.send(any()), times(1));
            }
        }
    }

    @Test
    void testSend_ShouldThrowRuntimeException_OnMessagingException() {
        // Mock Dotenv لcredentials صالحة
        try (MockedStatic<Dotenv> mockedDotenv = mockStatic(Dotenv.class)) {
            Dotenv mockEnv = mock(Dotenv.class);
            when(mockEnv.get("ADMIN_EMAIL")).thenReturn("test@gmail.com");
            when(mockEnv.get("ADMIN_PASS")).thenReturn("testpass");
            mockedDotenv.when(Dotenv::load).thenReturn(mockEnv);

            // Mock Transport لفشل MessagingException
            try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
                mockedTransport.when(() -> Transport.send(any(MimeMessage.class)))
                        .thenThrow(new MessagingException("535-5.7.8 Username and Password not accepted"));

                RealEmailServer server = new RealEmailServer();
                EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");

                // توقع رمي RuntimeException
                RuntimeException exception = assertThrows(RuntimeException.class, () -> server.send(message));
                assertTrue(exception.getMessage().contains("فشل إرسال الإيميل"));

                mockedTransport.verify(() -> Transport.send(any()), times(1));
            }
        }
    }

    @Test
    void testSend_ShouldThrowRuntimeException_OnUnexpectedException() {
        // Mock Dotenv لcredentials صالحة
        try (MockedStatic<Dotenv> mockedDotenv = mockStatic(Dotenv.class)) {
            Dotenv mockEnv = mock(Dotenv.class);
            when(mockEnv.get("ADMIN_EMAIL")).thenReturn("test@gmail.com");
            when(mockEnv.get("ADMIN_PASS")).thenReturn("testpass");
            mockedDotenv.when(Dotenv::load).thenReturn(mockEnv);

            // Mock Transport لاستثناء عام
            try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
                mockedTransport.when(() -> Transport.send(any(MimeMessage.class)))
                        .thenThrow(new RuntimeException("Network error"));

                RealEmailServer server = new RealEmailServer();
                EmailMessage message = new EmailMessage("recipient@example.com", "Test Subject", "Test Body");

                RuntimeException exception = assertThrows(RuntimeException.class, () -> server.send(message));
                assertTrue(exception.getMessage().contains("خطأ غير متوقع"));

                mockedTransport.verify(() -> Transport.send(any()), times(1));
            }
        }
    }
}