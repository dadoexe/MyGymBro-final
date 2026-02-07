package com.example.mygymbro.views;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.controller.LiveSessionController;

public interface LiveSessionView extends View {

    void setListener(LiveSessionController controller);

    // Mostra l'esercizio corrente con la GIF e i target
    void showExercise(WorkoutExerciseBean exercise, int currentSet, int totalSets);

    // Mostra la schermata di riposo col timer
    void showRestPhase(int seconds, String nextExerciseName);

    // Aggiorna il countdown del timer visivamente
    void updateTimerTick(String timeString);

    // Mostra il recap finale
    void showSessionRecap(String recapText);

    // Recupera i dati inseriti dall'utente (Rep e Peso fatti)
    int getInputReps();
    float getInputWeight();

    // Pulisce i campi di input per il prossimo set
    void clearInputFields();

    void updateSessionProgress(double progress);
    void runOnUiThread(Runnable action);
}