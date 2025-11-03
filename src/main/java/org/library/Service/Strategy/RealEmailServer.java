package org.library.Service.Strategy; // (1) تصحيح الحزمة لتناسب نمط الإشعارات

import org.library.Domain.EmailMessage; // (2) استيراد كائن القيمة من Domain Layer

/**
 * التطبيق الفعلي لخادم البريد الإلكتروني (Production, Sprint 3).
 * يقوم هذا الكلاس بمحاكاة إرسال البريد الإلكتروني.
 *
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 */
public class RealEmailServer implements EmailServer {

    /**
     * واجهة وهمية (Mock Interface) لتمثيل خادم البريد.
     * يجب تعريفها في مكان ما (يفضل org.library.service.notification).
     */
    public interface EmailServer {
        void send(EmailMessage email);
    }

    /**
     * يقوم بمحاكاة إرسال البريد الإلكتروني.
     * @param email كائن الرسالة المراد إرسالها.
     */
    @Override
    public void send(EmailMessage email) {
        // محاكاة عملية الإرسال
        System.out.println("--- EMAIL SENT ---");
        System.out.println("To: " + email.getRecipientEmail());
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Content: " + email.getContent());
        System.out.println("------------------");
    }
}