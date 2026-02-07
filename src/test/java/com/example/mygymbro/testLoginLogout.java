package com.example.mygymbro;

import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.controller.SessionManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testLoginLogout() {
        // 1. Setup: Simulo un utente loggato
        UserBean user = new UserBean();
        user.setUsername("mario_test");
        user.setRole("ATHLETE");
        user.setId(999);

        // 2. Action: Eseguo il login nel Singleton
        SessionManager.getInstance().login(user);

        // 3. Assert: Verifico che l'utente sia in sessione
        UserBean currentUser = SessionManager.getInstance().getCurrentUser();
        assertNotNull(currentUser, "L'utente dovrebbe essere loggato");
        assertEquals("mario_test", currentUser.getUsername());

        // 4. Action: Eseguo il logout
        SessionManager.getInstance().logout();

        // 5. Assert: Verifico che la sessione sia vuota
        assertNull(SessionManager.getInstance().getCurrentUser(), "Dopo il logout l'utente deve essere null");

        System.out.println("Test Sessione: SUPERATO ✅");
    }
}