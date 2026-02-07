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
        private Parent root;
        @FXML private ListView<WorkoutPlanBean> listWorkoutPlans;

        private NavigationController listener;

        @FXML
        public void initialize() {
            if (listWorkoutPlans != null) {
                listWorkoutPlans.getItems().clear();

                listWorkoutPlans.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
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

        // --- METODO REFACTORIZZATO (Cognitive Complexity bassissima) ---
        @Override
        public void updateWorkoutList(List<WorkoutPlanBean> workoutPlans) {
            Platform.runLater(() -> {
                updateListContent(workoutPlans);
                updateTotalCounter(workoutPlans);
                updateLastActivityDate(workoutPlans);
            });
        }

        // --- METODI HELPER PRIVATI (Spezzano la complessità) ---

        private void updateListContent(List<WorkoutPlanBean> workoutPlans) {
            if (listWorkoutPlans == null) return;

            listWorkoutPlans.getItems().clear();
            if (workoutPlans != null) {
                listWorkoutPlans.getItems().addAll(workoutPlans);
            }
        }

        private void updateTotalCounter(List<WorkoutPlanBean> workoutPlans) {
            if (lblTotalPlans == null) return;

            int count = (workoutPlans != null) ? workoutPlans.size() : 0;
            lblTotalPlans.setText(String.valueOf(count));
        }

        private void updateLastActivityDate(List<WorkoutPlanBean> workoutPlans) {
            if (lblLastActivity == null) return;

            if (workoutPlans == null || workoutPlans.isEmpty()) {
                lblLastActivity.setText("--");
                return;
            }

            WorkoutPlanBean last = workoutPlans.get(workoutPlans.size() - 1);
            if (last.getCreationDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                lblLastActivity.setText(sdf.format(last.getCreationDate()));
            } else {
                lblLastActivity.setText("Recente");
            }
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