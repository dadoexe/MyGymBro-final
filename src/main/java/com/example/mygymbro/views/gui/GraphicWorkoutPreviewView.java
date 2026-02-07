package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.PreviewController;
import com.example.mygymbro.views.WorkoutPreviewView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class GraphicWorkoutPreviewView implements WorkoutPreviewView, GraphicView {

    @FXML private Label lblPlanName;
    @FXML private Label lblDescription;
    @FXML private ListView<String> listExercises; // Semplice lista di stringhe per l'anteprima

    private PreviewController listener;
    private Parent root;

    @Override
    public void showPlanDetails(WorkoutPlanBean plan) {
        Platform.runLater(() -> {
            lblPlanName.setText(plan.getName());
            lblDescription.setText(plan.getComment());

            listExercises.getItems().clear();
            if (plan.getExerciseList() != null) {
                for (WorkoutExerciseBean ex : plan.getExerciseList()) {
                    // Formattazione carina della riga
                    String row = String.format("%s  |  %dx%d  |  Rec: %ds",
                            ex.getExerciseName(), ex.getSets(), ex.getReps(), ex.getRestTime());
                    listExercises.getItems().add(row);
                }
            }
        });
    }

    @FXML public void onStartSession() { listener.startSession(); }
    @FXML public void onBack() { listener.back(); }

    @Override public void setListener(PreviewController c) { this.listener = c; }
    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent root) { this.root = root; }
    @Override public void showError(String msg) {} // Alert opzionale
    @Override public void showSuccess(String msg) {}
}