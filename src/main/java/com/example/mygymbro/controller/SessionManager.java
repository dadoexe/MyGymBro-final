package com.example.mygymbro.controller;

import com.example.mygymbro.bean.UserBean;

public final class SessionManager {

    private UserBean currentUser;
    private static SessionManager instance = null;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(UserBean user) {
        this.currentUser = user;
    }
    public UserBean getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        // Metto a null l'utente corrente e pulisco eventuali altre cache
        this.currentUser = null;

    }
}