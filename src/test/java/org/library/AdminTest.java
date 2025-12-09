package org.library;

import org.junit.jupiter.api.Test;
import org.library.domain.Admin;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void shouldCreateAdminWithEmailAndPassword() {
        Admin admin = new Admin("admin@library.com", "secret123");

        assertEquals("admin@library.com", admin.getEmail());
        assertEquals("secret123", admin.getPassword());
    }

    @Test
    void shouldSetAndGetEmailCorrectly() {
        Admin admin = new Admin("old@email.com", "pass");

        admin.setEmail("new@library.com");

        assertEquals("new@library.com", admin.getEmail());
    }

    @Test
    void shouldSetAndGetPasswordCorrectly() {
        Admin admin = new Admin("admin@example.com", "oldpass");

        admin.setPassword("newSecurePass2025!");

        assertEquals("newSecurePass2025!", admin.getPassword());
    }

    @Test
    void shouldAllowNullValues_IfNeeded() {
        Admin admin = new Admin(null, null);

        assertNull(admin.getEmail());
        assertNull(admin.getPassword());

        admin.setEmail(null);
        admin.setPassword(null);

        assertNull(admin.getEmail());
        assertNull(admin.getPassword());
    }

    @Test
    void shouldHandleEmptyStrings() {
        Admin admin = new Admin("", "");

        assertEquals("", admin.getEmail());
        assertEquals("", admin.getPassword());
    }
}