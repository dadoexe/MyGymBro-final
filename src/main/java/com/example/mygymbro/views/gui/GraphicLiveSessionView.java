package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.controller.LiveSessionController;
import com.example.mygymbro.views.LiveSessionView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class GraphicLiveSessionView implements LiveSessionView, GraphicView {

    @FXML private VBox paneExercise;
    @FXML private VBox paneRest;

    // Header
    @FXML private ProgressBar progressBarSession;
    @FXML private Label lblProgressText;

    // Exercise Dashboard (Sostituisce GIF)
    @FXML private Label lblExerciseName;
    @FXML private Label lblMuscleGroup;
    @FXML private Label lblTargetReps;
    @FXML private Label lblTargetRest;
    @FXML private Label lblCurrentSet;

    // Inputs
    @FXML private TextField txtRepsInput;
    @FXML private TextField txtWeightInput;

    // Rest View
    @FXML private Label lblTimer;
    @FXML private Label lblNextExercise;

    private LiveSessionController listener;
    private Parent root;

    @Override
    public void showExercise(WorkoutExerciseBean exercise, int currentSet, int totalSets) {
        Platform.runLater(() -> {
            paneRest.setVisible(false);
            paneExercise.setVisible(true);

            // Popolamento Dashboard
            lblExerciseName.setText(exercise.getExerciseName());
            lblMuscleGroup.setText(exercise.getMuscleGroup().toUpperCase());

            lblCurrentSet.setText(currentSet + " / " + totalSets);
            lblTargetReps.setText(String.valueOf(exercise.getReps()));
            lblTargetRest.setText(exercise.getRestTime() + "s");

            // Focus e pulizia
            txtRepsInput.requestFocus();
        });
    }

    @Override
    public void updateSessionProgress(double progress) {
        Platform.runLater(() -> {
            progressBarSession.setProgress(progress);
            int percent = (int)(progress * 100);
            lblProgressText.setText(percent + "% Completato");
        });
    }

    @Override
    public void showRestPhase(int seconds, String nextExerciseName) {
        Platform.runLater(() -> {
            paneExercise.setVisible(false);
            paneRest.setVisible(true);

            lblTimer.setText(formatTime(seconds));
            lblNextExercise.setText(nextExerciseName);
        });
    }
    @Override
    public void runOnUiThread(Runnable action) {
        // In modalità Grafica, usiamo Platform.runLater
        javafx.application.Platform.runLater(action);
    }


    @Override
    public void updateTimerTick(String timeString) {
        Platform.runLater(() -> lblTimer.setText(timeString));
    }

    // --- ACTIONS ---
    @FXML public void onConfirmSet() { listener.confirmSet(); }
    @FXML public void onSkipRest() { listener.skipRest(); }
    @FXML public void onQuit() { listener.quit(); }

    // --- INPUT GETTERS ---
    @Override
    public int getInputReps() {
        return Integer.parseInt(txtRepsInput.getText());

    }

    @Override
    public float getInputWeight() {
        // Gestisce sia "50" che "50.5"
        return Float.parseFloat(txtWeightInput.getText().replace(",", "."));

    }

    @Override
    public void clearInputFields() {
        txtRepsInput.clear();
        txtWeightInput.clear();
    }

    @Override
    public void showSessionRecap(String recapText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Allenamento Concluso");
            alert.setHeaderText("Ottimo lavoro!");
            alert.setContentText(recapText);
            alert.showAndWait();
            listener.quit();
        });
    }

    // Helper interno per formattazione rapida iniziale
    private String formatTime(int s) { return String.format("%02d:%02d", s/60, s%60); }

    // ... metodi standard (setListener, showError, getRoot...) ...
    @Override public void setListener(LiveSessionController l) { this.listener = l; }
    @Override public Parent getRoot() { return root; }
    @Override public void setRoot(Parent r) { this.root = r; }
    @Override public void showError(String m) { /* Alert Error */ }
    @Override public void showSuccess(String m) { /* Alert Info */ }
}