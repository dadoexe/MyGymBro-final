package com.example.mygymbro.controller;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.views.LiveSessionView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiveSessionController implements Controller {

    private LiveSessionView view;
    private WorkoutPlanBean plan;
    private boolean isResting = false;
    private boolean sessionFinished = false;

    // Stato
    private int currentExerciseIndex = 0;
    private int currentSet = 1;
    private int totalTotalSets = 0; // Totale serie nell'intera scheda (per la progress bar)
    private int completedSets = 0;  // Serie completate finora

    private Timer timer;
    private int secondsRemaining;
    private List<String> sessionLog = new ArrayList<>();

    public LiveSessionController(LiveSessionView view, WorkoutPlanBean plan) {
        this.view = view;
        this.plan = plan;
        calculateTotalVolume(); // Calcoliamo quanto è lunga la scheda
    }

    private void calculateTotalVolume() {
        totalTotalSets = 0;
        if (plan != null && plan.getExerciseList() != null) {
            for (WorkoutExerciseBean ex : plan.getExerciseList()) {
                totalTotalSets += ex.getSets();
            }
        }
    }

    public void startSession() {
        if (plan == null || plan.getExerciseList().isEmpty()) {
            view.showError("Scheda vuota!");
            quit();
            return;
        }
        loadCurrentExerciseState();
    }

    private void loadCurrentExerciseState() {
        if (sessionFinished) return;
        if (currentExerciseIndex >= plan.getExerciseList().size()) {
            finishSession();
            return;
        }

        WorkoutExerciseBean ex = plan.getExerciseList().get(currentExerciseIndex);

        view.clearInputFields();
        view.showExercise(ex, currentSet, ex.getSets());
        updateProgress();
    }

    private void updateProgress() {
        if (totalTotalSets > 0) {
            double progress = (double) completedSets / totalTotalSets;
            view.updateSessionProgress(progress);
        }
    }

    // Chiamato quando l'utente preme "CONFERMA SET"
    public void confirmSet() {
        if (sessionFinished || isResting) return; // Impedisce doppi invii

        try {
            int actualReps = view.getInputReps();
            float actualWeight = view.getInputWeight();

            WorkoutExerciseBean ex = plan.getExerciseList().get(currentExerciseIndex);

            String logEntry = String.format("%s - Set %d: %d reps @ %.1f kg",
                    ex.getExerciseName(), currentSet, actualReps, actualWeight);
            sessionLog.add(logEntry);

            completedSets++;
            updateProgress();

            // --- CONTROLLO FINE ---
            boolean isLastExercise = (currentExerciseIndex == plan.getExerciseList().size() - 1);
            boolean isLastSetOfExercise = (currentSet == ex.getSets());

            if (isLastExercise && isLastSetOfExercise) {
                // FINE ALLENAMENTO: Niente timer, chiudiamo subito.
                finishSession();
            } else {
                // CONTINUA: Avvia riposo
                startRest(ex.getRestTime());
            }

        } catch (NumberFormatException e) {
            // CORREZIONE: Utilizzo della variabile 'e' per eliminare il Code Smell di SonarCloud
            view.showError("Inserisci numeri validi per Reps e Peso! Dettaglio: " + e.getMessage());
        }
    }

    private void startRest(int duration) {
        this.isResting = true;
        this.secondsRemaining = duration;

        String nextName = "Prossimo set";
        WorkoutExerciseBean currentEx = plan.getExerciseList().get(currentExerciseIndex);

        if (currentSet >= currentEx.getSets()) {
            // Se era l'ultimo set, controlla cosa c'è dopo
            if (currentExerciseIndex + 1 < plan.getExerciseList().size()) {
                nextName = "Prox: " + plan.getExerciseList().get(currentExerciseIndex + 1).getExerciseName();
            } else {
                nextName = "Fine Allenamento";
            }
        }

        view.showRestPhase(duration, nextName);

        // Timer
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsRemaining--;

                if (view != null) view.updateTimerTick(formatTime(secondsRemaining));

                if (secondsRemaining <= 0) {
                    timer.cancel();

                    // --- CORREZIONE QUI ---
                    // NON usare Platform.runLater direttamente!
                    // Usiamo il metodo della view che sa se siamo in CLI o GUI.
                    if (view != null) {
                        view.runOnUiThread(() -> skipRest());
                    }
                    // ----------------------
                }
            }
        }, 0, 1000);
    }

    public void skipRest() {
        if (timer != null) { timer.cancel(); timer = null; }
        this.isResting = false;

        if (sessionFinished) return;

        // Controllo preventivo: se siamo già fuori range, non leggiamo nulla
        if (currentExerciseIndex >= plan.getExerciseList().size()) {
            finishSession();
            return;
        }

        WorkoutExerciseBean currentEx = plan.getExerciseList().get(currentExerciseIndex);

        if (currentSet < currentEx.getSets()) {
            currentSet++;
        } else {
            currentSet = 1;
            currentExerciseIndex++;
        }

        loadCurrentExerciseState();
    }

    private void finishSession() {
        sessionFinished = true; // Blocco tutto
        if (timer != null) timer.cancel();

        StringBuilder sb = new StringBuilder("Riepilogo Sessione:\n\n");
        for (String line : sessionLog) {
            sb.append(line).append("\n");
        }
        view.showSessionRecap(sb.toString());
    }

    public void quit() {
        if (timer != null) timer.cancel();
        ApplicationController.getInstance().loadHomeBasedOnRole();
    }

    private String formatTime(int seconds) {
        if (seconds < 0) return "00:00";
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public void dispose() {
        if (timer != null) timer.cancel();
    }
}