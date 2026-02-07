package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.LoginController;
import com.example.mygymbro.controller.TrainerController;
import com.example.mygymbro.views.TrainerView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.List;

public class GraphicTrainerView implements TrainerView, GraphicView {

    private TrainerController listener;
    private Parent root;

    // --- RIFERIMENTI FXML (Devono coincidere con fx:id nel file fxml) ---
    @FXML private Label lblWelcome;
    @FXML private Label lblSelectedClient;

    @FXML private ListView<AthleteBean> listAthletes;     // Lista Sinistra
    @FXML private ListView<WorkoutPlanBean> listWorkoutPlans; // Lista Destra

    @FXML private Button btnAddPlan;
    @FXML private Button btnEditPlan;

    /**
     * Chiamato automaticamente da JavaFX all'avvio
     */
    @FXML
    public void initialize() {
        // --- 1. CONFIGURA LA LISTA CLIENTI (Visualizzazione) ---
        // Questo serve a dire alla lista: "Non stampare l'oggetto, stampa Nome e Cognome!"
        listAthletes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AthleteBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Esempio: "mario (Mario Rossi)"
                    setText(item.getUsername() + " (" + item.getNome() + " " + item.getCognome() + ")");
                    // Stile personalizzato per la cella
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5;");
                }
            }
        });

        // --- 2. LISTENER SELEZIONE CLIENTE ---
        // Cosa succede quando clicco su un cliente?
        listAthletes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Cliente selezionato
                btnAddPlan.setDisable(false); // Ora puoi creare una scheda
                lblSelectedClient.setText("Cliente: " + newVal.getNome() + " " + newVal.getCognome());

                // Chiediamo al Controller di scaricare le schede di QUESTO cliente
                if (listener != null) {
                    listener.loadPlansForAthlete(newVal);
                }
            } else {
                // Nessuna selezione
                btnAddPlan.setDisable(true);
                lblSelectedClient.setText("Seleziona un cliente...");
                listWorkoutPlans.getItems().clear(); // Pulisci la destra
            }
        });

        // --- 3. CONFIGURA LA LISTA SCHEDE (Visualizzazione) ---
        listWorkoutPlans.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WorkoutPlanBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Esempio: "Scheda Massa - Commento..."
                    String text = item.getName();
                    if (item.getComment() != null && !item.getComment().isEmpty()) {
                        text += " (" + item.getComment() + ")";
                    }
                    setText(text);
                    setStyle("-fx-text-fill: #00E676; -fx-font-weight: bold;"); // Verde Matrix
                }
            }
        });

        // Listener selezione scheda (per abilitare il tasto modifica)
        listWorkoutPlans.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Abilita "Modifica" solo se ho selezionato una scheda
            btnEditPlan.setDisable(newVal == null);
        });
    }

    // --- AZIONI BOTTONI FXML ---

    @FXML
    public void handleCreatePlan() {
        if (listener != null) listener.createNewPlan();
    }

    @FXML
    public void handleEditPlan() {
        if (listener != null) listener.modifySelectedPlan();
    }

    @FXML
    public void handleLogout() {
        if (listener != null) listener.logout();
    }

    // --- METODI INTERFACCIA TrainerView ---

    @Override
    public void setListener(TrainerController listener) {
        this.listener = listener;
    }

    @Override
    public void showAthletesList(List<AthleteBean> athletes) {
        // Aggiornamento grafico sicuro
        Platform.runLater(() -> {
            listAthletes.getItems().clear();
            if (athletes != null) {
                listAthletes.getItems().addAll(athletes);
            }
        });
    }

    @Override
    public void showAthletePlans(List<WorkoutPlanBean> plans) {
        Platform.runLater(() -> {
            listWorkoutPlans.getItems().clear();
            if (plans != null) {
                listWorkoutPlans.getItems().addAll(plans);
            }
        });
    }

    @Override
    public void updateWelcomeMessage(String msg) {
        Platform.runLater(() -> {
            if (lblWelcome != null) lblWelcome.setText("DASHBOARD TRAINER: " + msg);
        });
    }

    // --- GETTERS PER IL CONTROLLER ---

    @Override
    public AthleteBean getSelectedAthlete() {
        return listAthletes.getSelectionModel().getSelectedItem();
    }

    @Override
    public WorkoutPlanBean getSelectedPlan() {
        return listWorkoutPlans.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setSelectedAthlete(AthleteBean athlete) {
        return;
    }

    // --- METODI MESSAGGI & VIEW ---

    @Override
    public void showSuccess(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
            alert.showAndWait();
        });
    }

    @Override
    public void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg);
            alert.showAndWait();
        });
    }

    // Metodo legacy updateAthletePrograms (se presente nell'interfaccia vecchia, lo mappiamo al nuovo)

    public void updateAthletePrograms(List<WorkoutPlanBean> workoutPlans) {
        showAthletePlans(workoutPlans);
    }

    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent root) { this.root = root; }
}