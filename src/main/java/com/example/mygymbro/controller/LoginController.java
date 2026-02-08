package com.example.mygymbro.controller;

import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.UserDAO;
import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.exceptions.InvalidCredentialsException;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import com.example.mygymbro.views.LoginView;


import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController implements Controller {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private static final String TRAINER_ROLE = "TRAINER";
    private static final String ATHLETE_ROLE = "ATHLETE";

    private UserDAO userDAO;
    private LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
        this.userDAO = DAOFactory.getUserDAO();
    }

    public void checkLogin() {
        String username = view.getUsername();
        String password = view.getPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            view.showError("Inserisci username e password.");
            return;
        }

        try {
            // 1. Chiediamo al DAO (Database)
            User userModel = userDAO.findByUsername(username);

            // 2. LOGICA DI BUSINESS CON ECCEZIONE
            // Se l'utente non esiste O la password non corrisponde -> LANCIA ECCEZIONE
            if (userModel == null || !userModel.getPassword().equals(password)) {
                throw new InvalidCredentialsException("Credenziali non valide (Username o Password errati).");
            }

            // --- Se siamo qui, il login è valido ---

            // 3. Riempi il Bean
            UserBean userBean = createUserBean(userModel);

            // 4. Login in Sessione e Cambio Schermata
            SessionManager.getInstance().login(userBean);
            view.showSuccess("Login effettuato! Ruolo: " + userBean.getRole());
            ApplicationController.getInstance().loadHomeBasedOnRole();

        } catch (InvalidCredentialsException e) {
            // REQUISITO SODDISFATTO: Catturiamo la nostra eccezione personalizzata
            LOGGER.log(Level.WARNING, "Tentativo di login fallito per username: {0}", username);
            view.showError("Accesso Negato: " + e.getMessage());

        }catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore database durante il login", e);
            view.showError("Errore tecnico del Database.");
        }
    }

    private UserBean createUserBean(User userModel) {
        UserBean userBean = new UserBean();
        userBean.setId(userModel.getId());
        userBean.setUsername(userModel.getUsername());
        userBean.setNome(userModel.getName());
        userBean.setCognome(userModel.getCognome());
        userBean.setEmail(userModel.getEmail());

        // Assegna Ruolo
        if (userModel instanceof PersonalTrainer) {
            userBean.setRole(TRAINER_ROLE);
        } else {
            userBean.setRole(ATHLETE_ROLE);
        }

        return userBean;
    }

    @Override
    public void dispose() {
        this.userDAO = null;
    }
}