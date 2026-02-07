package com.example.mygymbro.views.gui;

import com.example.mygymbro.controller.ApplicationController;
import com.example.mygymbro.dao.DAOFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LauncherController {

    // 1. LOGGER: Sostituisce System.out
    private static final Logger logger = Logger.getLogger(LauncherController.class.getName());

    @FXML private Button btnDemo;
    private boolean isDemoMode = false;

    @FXML
    public void startGui(ActionEvent event) {
        // 1. Configura l'app in modalità GRAFICA
        DAOFactory.setDemoMode(isDemoMode);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ApplicationController.getInstance().configure(true, stage);

        // 2. Avvia
        ApplicationController.getInstance().start();
    }

    @FXML
    public void startCli(ActionEvent event) {
        // 1. Chiudi la finestra del launcher
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

        // 2. Configura l'app in modalità TESTUALE
        DAOFactory.setDemoMode(isDemoMode);

        // CORREZIONE: Uso del logger invece di System.out
        logger.log(Level.INFO, ">>> AVVIO CLI IN CORSO... GUARDA LA CONSOLE! <<<");

        ApplicationController.getInstance().configure(false, null);

        // 3. Avvia
        ApplicationController.getInstance().start();
    }

    @FXML
    public void toggleDemo(ActionEvent event) {
        isDemoMode = !isDemoMode;

        // CORREZIONE: Chiamata al metodo statico nella Factory (se esistente)
        // Se non hai ancora implementato setDemoMode in DAOFactory, puoi farlo ora.
        DAOFactory.setDemoMode(isDemoMode);

        if (isDemoMode) {
            btnDemo.setText("DEMO MODE: ON");
            btnDemo.setStyle("-fx-background-color: #00E676; -fx-text-fill: black;");
        } else {
            btnDemo.setText("DEMO MODE: OFF");
            btnDemo.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white;");
        }
    }
}