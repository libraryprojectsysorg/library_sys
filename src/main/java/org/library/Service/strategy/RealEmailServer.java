package org.library.Service.strategy;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.library.domain.EmailMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

/**
 * @author Weam Ahmad
 * @author Seba Abd Aljwwad
 * @version 2.2  // محدث لـ 2025: App Password + graceful handling
 */
public class RealEmailServer implements EmailServer {

    private static final Logger logger = Logger.getLogger(RealEmailServer.class.getName());

    private final String adminEmail;
    private final String adminPass;

    public RealEmailServer() {
        Dotenv dotenv = Dotenv.load();
        this.adminEmail = dotenv.get("ADMIN_EMAIL");
        this.adminPass = dotenv.get("ADMIN_PASS");


        if (adminEmail == null || adminPass == null || adminPass.trim().isEmpty()) {
            logger.severe("❌ ADMIN_EMAIL أو ADMIN_PASS غير صالح في .env. استخدم App Password صالح.");
            throw new IllegalStateException("إعدادات الإيميل ناقصة. تحقق من .env (App Password مطلوب لـ Gmail 2025).");
        }
    }

    @Override
    public void send(EmailMessage email) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(adminEmail, adminPass);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(adminEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipientEmail()));
            message.setSubject(email.getSubject());
            message.setText(email.getContent());

            Transport.send(message);
            logger.log(Level.INFO, "✅ Email sent successfully to: " + email.getRecipientEmail());
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "❌ Failed to send email: " + e.getMessage() + " | تحقق من App Password (Gmail 2025 يتطلب 2FA + App Password).", e);
            throw new RuntimeException("فشل إرسال الإيميل: " + e.getMessage() + ". استخدم App Password صالح أو Outlook.", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Unexpected error in email sending: " + e.getMessage(), e);
            throw new RuntimeException("خطأ غير متوقع في الإيميل: " + e.getMessage(), e);
        }
    }
}