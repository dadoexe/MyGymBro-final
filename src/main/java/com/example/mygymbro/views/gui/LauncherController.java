package com.example.mygymbro.views.gui;

import com.example.mygymbro.controller.ApplicationController;
import com.example.mygymbro.dao.DAOFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LauncherController {

    @FXML private Button btnDemo;
    private boolean isDemoMode = false;

    @FXML
    public void startGui(ActionEvent event) {
        // 1. Configura l'app in modalità GRAFICA
        DAOFactory.setDemoMode(isDemoMode);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        ApplicationController.getInstance().configure(true, stage);

        // 2. Avvia
        ApplicationController.getInstance().start();
    }

    @FXML
    public void startCli(ActionEvent event) {
        // 1. Chiudi la finestra del launcher (la CLI sta nella console!)
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();

        // 2. Configura l'app in modalità TESTUALE
        System.out.println(">>> AVVIO CLI IN CORSO... GUARDA LA CONSOLE! <<<");
        ApplicationController.getInstance().configure(false, null);

        // 3. Avvia
        ApplicationController.getInstance().start();
    }

    @FXML
    public void toggleDemo(ActionEvent event) {
        isDemoMode = !isDemoMode;
        // Qui dovresti avere un metodo statico nella tua Factory per attivare i dati finti
        // DAOFactory.setDemoMode(isDemoMode);

        if (isDemoMode) {
            btnDemo.setText("DEMO MODE: ON");
            btnDemo.setStyle("-fx-background-color: #00E676; -fx-text-fill: black;");
        } else {
            btnDemo.setText("DEMO MODE: OFF");
            btnDemo.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white;");
        }
    }
}