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
 * @author  Seba Abd Aljwwad
 * @version 2.0
 */
public class RealEmailServer implements EmailServer {

    private static final Logger logger = Logger.getLogger(RealEmailServer.class.getName());

    private final String adminEmail;
    private final String adminPass;

    public RealEmailServer() {
        Dotenv dotenv = Dotenv.load();
        this.adminEmail = dotenv.get("ADMIN_EMAIL");
        this.adminPass = dotenv.get("ADMIN_PASS");
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
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email.getRecipientEmail()));
            message.setSubject(email.getSubject());
            message.setText(email.getContent());

            Transport.send(message);
            logger.log(Level.INFO, "✅ Email sent successfully to: " + email.getRecipientEmail());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Failed to send email: " + e.getMessage(), e);
        }
    }
}