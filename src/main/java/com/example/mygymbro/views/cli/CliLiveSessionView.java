package com.example.mygymbro.views.cli;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.controller.LiveSessionController;
import com.example.mygymbro.views.LiveSessionView;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliLiveSessionView implements LiveSessionView, CliView {

    private static final Logger LOGGER = Logger.getLogger(CliLiveSessionView.class.getName());

    private LiveSessionController listener;
    private final Scanner scanner;
    private int inputReps;
    private float inputWeight;
    private boolean sessionRunning = true;
    private boolean inRestPhase = false;

    public CliLiveSessionView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        System.out.println("\n=== LIVE SESSION ===");
        if (listener != null) listener.startSession();

        while (sessionRunning) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // Ripristina lo stato di interruzione
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Sessione live interrotta", e);
                // Esci dal ciclo se il thread viene interrotto
                break;
            }
        }
    }

    @Override
    public void showExercise(WorkoutExerciseBean exercise, int currentSet, int totalSets) {
        inRestPhase = false;
        System.out.println("\n🏋️ " + exercise.getExerciseName() + " (" + currentSet + "/" + totalSets + ")");
        System.out.println("Target: " + exercise.getReps() + " reps");

        // Input protetto
        int reps = askInt("Reps fatte ('q' esci) > ");
        if (reps == -1) {
            handleQuit();
            return;
        }

        float weight = askFloat("Carico kg ('q' esci) > ");
        if (weight == -1) {
            handleQuit();
            return;
        }

        this.inputReps = reps;
        this.inputWeight = weight;

        System.out.println("✅ Premi INVIO per recuperare...");
        scanner.nextLine(); // Consuma invio

        if (listener != null) listener.confirmSet();
    }

    @Override
    public void showRestPhase(int seconds, String nextExerciseName) {
        inRestPhase = true;
        System.out.println("☕ RECUPERO: " + seconds + "s (Prox: " + nextExerciseName + ")");
        System.out.println("(Attendi la fine del timer...)");
    }

    @Override
    public void updateTimerTick(String timeString) {
        if (inRestPhase) System.out.print("\r⏳ " + timeString + "   ");
    }

    @Override
    public void runOnUiThread(Runnable action) {
        inRestPhase = false;
        System.out.println(); // A capo dopo il timer
        action.run();
    }

    @Override
    public void showSessionRecap(String recapText) {
        sessionRunning = false;
        inRestPhase = false;
        System.out.println("\n🏆 FINE SESSIONE!\n" + recapText);
        System.out.println("\nPremi INVIO per tornare al menu.");

        // FIX BUFFER: Assicuriamoci di leggere un invio pulito
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        if (listener != null) listener.quit();
    }

    private void handleQuit() {
        sessionRunning = false;
        if (listener != null) listener.quit();
    }

    // Metodi helper robusti contro loop infiniti
    private int askInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("q")) return -1;
            if (line.isEmpty()) continue;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException numberFormatException) {
                System.out.print("Numero non valido: ");
            }
        }
    }

    private float askFloat(String prompt) {
        System.out.print(prompt);
        while (true) {
            String line = scanner.nextLine().trim().replace(",", ".");
            if (line.equalsIgnoreCase("q")) return -1;
            if (line.isEmpty()) continue;
            try {
                return Float.parseFloat(line);
            } catch (NumberFormatException numberFormatException) {
                System.out.print("Numero non valido: ");
            }
        }
    }

    // Getter standard...
    @Override
    public int getInputReps() {
        return inputReps;
    }

    @Override
    public float getInputWeight() {
        return inputWeight;
    }

    @Override
    public void clearInputFields() {
        // Intenzionalmente vuoto: Nella CLI l'input è sequenziale, non ci sono campi da pulire.
    }

    @Override
    public void updateSessionProgress(double progress) {
        // Intenzionalmente vuoto: La CLI non ha una progress bar visuale.
    }

    @Override
    public void setListener(LiveSessionController controller) {
        this.listener = controller;
    }

    @Override
    public void showError(String message) {
        System.out.println("❌ " + message);
    }

    @Override
    public void showSuccess(String message) {
        // Già che ci siamo, stampiamolo! Risolve il problema del parametro inutilizzato.
        System.out.println("✅ " + message);
    }
}