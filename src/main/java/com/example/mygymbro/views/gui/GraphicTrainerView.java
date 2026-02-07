package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
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

    // --- ELEMENTI FXML ---
    @FXML private Label lblWelcome;
    @FXML private Label lblSelectedClient;

    @FXML private ListView<AthleteBean> listAthletes;
    @FXML private ListView<WorkoutPlanBean> listWorkoutPlans;

    @FXML private Button btnAddPlan;
    @FXML private Button btnEditPlan;

    /**
     * Inizializzazione automatica JavaFX.
     * Logica suddivisa in metodi privati per soddisfare SonarCloud.
     */
    @FXML
    public void initialize() {
        configureAthleteListCellFactory();
        configurePlanListCellFactory();
        setupSelectionListeners();
    }

    // --- 1. CONFIGURAZIONE GRAFICA LISTE (Private) ---

    private void configureAthleteListCellFactory() {
        if (listAthletes == null) return;

        listAthletes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AthleteBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Es: "Mario Rossi (mario)"
                    setText(item.getNome() + " " + item.getCognome() + " (" + item.getUsername() + ")");
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5;");
                }
            }
        });
    }

    private void configurePlanListCellFactory() {
        if (listWorkoutPlans == null) return;

        listWorkoutPlans.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WorkoutPlanBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Es: "Scheda Massa"
                    setText(item.getName());
                    setStyle("-fx-text-fill: #00E676; -fx-font-weight: bold; -fx-font-size: 13px;");
                }
            }
        });
    }

    // --- 2. GESTIONE EVENTI SELEZIONE (Private) ---

    private void setupSelectionListeners() {
        // Listener Selezione Atleta
        if (listAthletes != null) {
            listAthletes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    // 1. Abilita UI
                    if (btnAddPlan != null) btnAddPlan.setDisable(false);
                    updateSelectedClientInfo(newVal);

                    // 2. Chiama il metodo specifico del Controller
                    if (listener != null) {
                        listener.loadPlansForAthlete(newVal);
                    }
                } else {
                    // Deselezione
                    if (btnAddPlan != null) btnAddPlan.setDisable(true);
                    updateSelectedClientInfo(null);
                    if (listWorkoutPlans != null) listWorkoutPlans.getItems().clear();
                }
            });
        }

        // Listener Selezione Scheda (per abilitare Modifica)
        if (listWorkoutPlans != null) {
            listWorkoutPlans.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (btnEditPlan != null) {
                    btnEditPlan.setDisable(newVal == null);
                }
            });
        }
    }

    // --- 3. EVENTI BOTTONI (Chiamati da FXML onAction) ---

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

    // --- 4. IMPLEMENTAZIONE INTERFACCIA TRAINERVIEW ---

    @Override
    public void setListener(TrainerController listener) {
        this.listener = listener;
    }

    @Override
    public void showAthletesList(List<AthleteBean> athletes) {
        Platform.runLater(() -> {
            if (listAthletes != null) {
                listAthletes.getItems().clear();
                if (athletes != null) {
                    listAthletes.getItems().addAll(athletes);
                }
            }
        });
    }

    @Override
    public void showAthletePlans(List<WorkoutPlanBean> plans) {
        Platform.runLater(() -> {
            if (listWorkoutPlans != null) {
                listWorkoutPlans.getItems().clear();
                if (plans != null) {
                    listWorkoutPlans.getItems().addAll(plans);
                }
            }
        });
    }

    @Override
    public void updateWelcomeMessage(String msg) {
        Platform.runLater(() -> {
            if (lblWelcome != null) lblWelcome.setText("Trainer: " + msg);
        });
    }


    public void updateSelectedClientInfo(AthleteBean athlete) {
        Platform.runLater(() -> {
            if (lblSelectedClient != null) {
                if (athlete != null) {
                    lblSelectedClient.setText("Cliente: " + athlete.getNome() + " " + athlete.getCognome());
                } else {
                    lblSelectedClient.setText("Seleziona un cliente...");
                }
            }
        });
    }

    @Override
    public AthleteBean getSelectedAthlete() {
        if (listAthletes == null) return null;
        return listAthletes.getSelectionModel().getSelectedItem();
    }

    @Override
    public WorkoutPlanBean getSelectedPlan() {
        if (listWorkoutPlans == null) return null;
        return listWorkoutPlans.getSelectionModel().getSelectedItem();
    }

    // --- FIX: IMPLEMENTAZIONE MANCANTE ---
    @Override
    public void setSelectedAthlete(AthleteBean athlete) {
        if (athlete == null || listAthletes == null) return;

        Platform.runLater(() -> {
            // Cerchiamo l'atleta nella lista esistente tramite ID
            for (AthleteBean a : listAthletes.getItems()) {
                if (a.getId() == athlete.getId()) {
                    listAthletes.getSelectionModel().select(a);
                    listAthletes.scrollTo(a);
                    break;
                }
            }
        });
    }

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

    // --- GraphicView Interface ---
    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent root) { this.root = root; }
}