package com.example.mygymbro.views.gui;

import com.example.mygymbro.controller.LoginController;
import com.example.mygymbro.views.LoginView;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class GraphicLoginView implements LoginView, GraphicView {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private LoginController listener;
    private Parent root;

    @FXML
    public void initialize() {
        if (btnLogin != null) {
            btnLogin.setOnAction(event -> handleLogin());
        }
    }

    private void handleLogin() {
        if (listener != null) listener.checkLogin();
    }

    @Override
    public String getUsername() { return txtUsername.getText(); }

    @Override
    public String getPassword() { return txtPassword.getText(); }

    @Override
    public void setListener(LoginController listener) { this.listener = listener; }

    // --- IMPLEMENTAZIONE NUOVI METODI View ---

    @Override
    public void showError(String message) {
        // Mostra l'errore nella label rossa sopra il bottone (più elegante per il login)
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    @Override
    public void showSuccess(String message) {
        // Per il login di solito non serve, ma se servisse usiamo un Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Metodi GraphicView ---
    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent root) { this.root = root; }
}
