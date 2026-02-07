package com.example.mygymbro.controller;

import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.UserDAO;
import com.example.mygymbro.exceptions.InvalidCredentialsException; // <--- Importante!
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import com.example.mygymbro.views.LoginView;

import java.sql.SQLException;

public class LoginController implements Controller {

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
            UserBean userBean = new UserBean();
            userBean.setId(userModel.getId());
            userBean.setUsername(userModel.getUsername());
            userBean.setNome(userModel.getName());
            userBean.setCognome(userModel.getCognome());
            userBean.setEmail(userModel.getEmail());

            // 4. Assegna Ruolo
            if (userModel instanceof PersonalTrainer) {
                userBean.setRole("TRAINER");
            } else {
                userBean.setRole("ATHLETE");
            }

            // 5. Login in Sessione e Cambio Schermata
            SessionManager.getInstance().login(userBean);
            view.showSuccess("Login effettuato! Ruolo: " + userBean.getRole());
            ApplicationController.getInstance().loadHomeBasedOnRole();

        } catch (InvalidCredentialsException e) {
            // REQUISITO SODDISFATTO: Catturiamo la nostra eccezione personalizzata
            view.showError("Accesso Negato: " + e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();
            view.showError("Errore tecnico del Database.");
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Errore generico di sistema.");
        }
    }

    @Override
    public void dispose() {
        this.userDAO = null;
    }
}