package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.NavigationController;
import com.example.mygymbro.views.AthleteView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.text.SimpleDateFormat;
import java.util.List;

public class GraphicAthleteView implements AthleteView, GraphicView {

    @FXML private Label lblWelcome;
    @FXML private Label lblTotalPlans;
    @FXML private Label lblLastActivity;

    // --- CORREZIONE: TENIAMO SOLO QUESTA VARIABILE ---
    @FXML private ListView<WorkoutPlanBean> listWorkoutPlans;

    private NavigationController listener;
    private Parent root;

    @FXML
    public void initialize() {
        // Pulisce la lista all'avvio per sicurezza
        if (listWorkoutPlans != null) {
            listWorkoutPlans.getItems().clear();

            // --- GESTIONE DOPPIO CLIC (Aggiornata per usare listWorkoutPlans) ---
            listWorkoutPlans.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { // 2 click veloci
                    WorkoutPlanBean selected = listWorkoutPlans.getSelectionModel().getSelectedItem();
                    if (selected != null && listener != null) {
                        listener.openPlanPreview(selected);
                    }
                }
            });
        }
    }

    @Override
    public void setListener(NavigationController listener) {
        this.listener = listener;
    }

    @Override
    public void updateWelcomeMessage(String msg) {
        if (lblWelcome != null) lblWelcome.setText("Benvenuto " + msg + "!");
    }

    // --- METODO AGGIORNATO E SICURO ---
    @Override
    public void updateWorkoutList(List<WorkoutPlanBean> workoutPlans) {
        Platform.runLater(() -> {
            // 1. Aggiorna la lista centrale
            if (listWorkoutPlans != null) {
                listWorkoutPlans.getItems().clear();
                if (workoutPlans != null) {
                    listWorkoutPlans.getItems().addAll(workoutPlans);
                }
            }

            // 2. Aggiorna le Statistiche in alto
            if (lblTotalPlans != null) {
                lblTotalPlans.setText(String.valueOf(workoutPlans != null ? workoutPlans.size() : 0));
            }

            if (lblLastActivity != null) {
                if (workoutPlans != null && !workoutPlans.isEmpty()) {
                    WorkoutPlanBean last = workoutPlans.get(workoutPlans.size() - 1);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    // Controllo null sulla data
                    String dateStr = (last.getCreationDate() != null) ? sdf.format(last.getCreationDate()) : "Recente";
                    lblLastActivity.setText(dateStr);
                } else {
                    lblLastActivity.setText("--");
                }
            }
        });
    }

    // --- BOTTONI ---

    @FXML
    public void handleCreatePlan(ActionEvent event) {
        if (listener != null) listener.handleCreateNewPlan();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (listener != null) listener.logout();
    }

    @FXML
    public void handleDeletePlan(ActionEvent event) {
        if (listWorkoutPlans == null) return;
        WorkoutPlanBean selected = listWorkoutPlans.getSelectionModel().getSelectedItem();
        if (selected != null && listener != null) {
            listener.deletePlan(selected);
        } else {
            showError("Seleziona una scheda da eliminare.");
        }
    }

    @FXML
    public void handleEditPlan(ActionEvent event) {
        if (listWorkoutPlans == null) return;
        WorkoutPlanBean selected = listWorkoutPlans.getSelectionModel().getSelectedItem();
        if (selected != null && listener != null) {
            listener.modifyPlan(selected);
        } else {
            showError("Seleziona una scheda da modificare.");
        }
    }


    // --- MESSAGGI ---

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- GraphicView ---
    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent root) { this.root = root; }
}