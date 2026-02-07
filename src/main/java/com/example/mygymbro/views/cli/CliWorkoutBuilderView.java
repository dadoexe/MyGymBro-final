package com.example.mygymbro.views.cli;

import com.example.mygymbro.bean.ExerciseBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.PlanManagerController;
import com.example.mygymbro.views.WorkoutBuilderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CliWorkoutBuilderView implements WorkoutBuilderView, CliView {

    private PlanManagerController listener;
    private final Scanner scanner;

    // Stato locale della scheda
    private String planName = "";
    private String planComment = "";
    private List<WorkoutExerciseBean> addedExercises = new ArrayList<>(); // Lista locale degli esercizi

    public CliWorkoutBuilderView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        System.out.println("\n=================================");
        System.out.println("   WORKOUT BUILDER (CLI MODE)   ");
        System.out.println("=================================");

        // 1. Chiediamo subito i dati della scheda se non ci sono (Creazione)
        if (planName == null || planName.isEmpty()) {
            inputHeaderData();
        } else {
            // Se ci sono già (Modifica), li mostriamo
            System.out.println("Modifica scheda: " + planName);
            System.out.println("Commento: " + planComment);
            printTable(); // Mostriamo subito gli esercizi esistenti
        }

        // 2. Loop principale del menu
        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU BUILDER ---");
            System.out.println("1. Modifica Nome/Descrizione");
            System.out.println("2. Cerca e Aggiungi Esercizio (da API)"); // <--- TORNA LA RICERCA!
            System.out.println("3. Rimuovi Esercizio");
            System.out.println("4. Visualizza Riepilogo Esercizi");
            System.out.println("5. SALVA SCHEDA");
            System.out.println("0. Annulla / Esci senza salvare");
            System.out.print("Scelta > ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    inputHeaderData();
                    break;
                case "2":
                    handleSearchFlow(); // <--- Metodo ripristinato
                    break;
                case "3":
                    removeExercise();
                    break;
                case "4":
                    printTable();
                    break;
                case "5":
                    System.out.println("Salvataggio in corso...");
                    if (listener != null) listener.handleSavePlan();
                    running = false;
                    break;
                case "0":
                    System.out.println("Annullamento...");
                    if (listener != null) listener.handleCancel();
                    running = false;
                    break;
                default:
                    System.out.println("Scelta non valida, riprova.");
            }
        }
    }

    // --- LOGICA DI INPUT E RICERCA ---

    private void inputHeaderData() {
        System.out.print("Inserisci Nome Scheda: ");
        this.planName = scanner.nextLine();
        System.out.print("Inserisci Commento/Descrizione: ");
        this.planComment = scanner.nextLine();
        System.out.println("Dati aggiornati.");
    }

    private void handleSearchFlow() {
        System.out.print("\nCerca esercizio (es. 'Bench', 'Squat'): ");
        String query = scanner.nextLine();

        if (listener == null) return;

        // 1. Chiediamo al controller di cercare online
        System.out.println("(Ricerca API in corso...)");
        List<ExerciseBean> results = listener.searchExercisesOnApi(query);

        if (results.isEmpty()) {
            System.out.println("⚠️ Nessun esercizio trovato per: " + query);
            return;
        }

        // 2. Mostriamo i risultati numerati
        System.out.println("\nRisultati trovati:");
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%d. %s (%s)\n", (i + 1), results.get(i).getName(), results.get(i).getMuscleGroup());
        }

        // 3. Selezione utente
        System.out.print("Seleziona il numero da aggiungere (0 per annullare): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx > 0 && idx <= results.size()) {
                ExerciseBean selected = results.get(idx - 1);
                askDetailsAndAdd(selected);
            } else if (idx != 0) {
                System.out.println("Numero non valido.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido. Inserisci un numero.");
        }
    }

    private void askDetailsAndAdd(ExerciseBean exercise) {
        try {
            System.out.println("\nImposta i dettagli per: " + exercise.getName());
            System.out.print("Sets: ");
            int sets = Integer.parseInt(scanner.nextLine());

            System.out.print("Reps: ");
            int reps = Integer.parseInt(scanner.nextLine());

            System.out.print("Recupero (secondi): ");
            int rest = Integer.parseInt(scanner.nextLine());

            // 1. Creiamo il bean
            WorkoutExerciseBean wb = new WorkoutExerciseBean();
            wb.setExerciseName(exercise.getName());
            wb.setMuscleGroup(exercise.getMuscleGroup());
            wb.setSets(sets);
            wb.setReps(reps);
            wb.setRestTime(rest);

            // 2. Passiamo il bean al Controller
            if (listener != null) {
                listener.addExerciseToPlan(wb);
            }

        } catch (NumberFormatException e) {
            System.out.println("Errore: devi inserire numeri interi validi.");
        }
    }

    private void removeExercise() {
        if (addedExercises.isEmpty()) {
            System.out.println("Nessun esercizio da rimuovere.");
            return;
        }
        printTable();
        System.out.print("Inserisci numero da rimuovere: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < addedExercises.size()) {
                WorkoutExerciseBean removed = addedExercises.get(index);
                if (listener != null) listener.removeExerciseFromPlan(removed);
                System.out.println("Esercizio rimosso.");
            }
        } catch (Exception e) {
            System.out.println("Errore input.");
        }
    }

    private void printTable() {
        System.out.println("\n--- LISTA ESERCIZI ATTUALE ---");
        if (addedExercises == null || addedExercises.isEmpty()) {
            System.out.println("(Nessun esercizio aggiunto)");
        } else {
            System.out.printf("%-3s | %-25s | %-5s | %-5s | %-5s\n", "N.", "Nome", "Sets", "Reps", "Rec");
            System.out.println("-------------------------------------------------------------");
            for (int i = 0; i < addedExercises.size(); i++) {
                WorkoutExerciseBean wb = addedExercises.get(i);
                System.out.printf("%-3d | %-25s | %-5d | %-5d | %ds\n",
                        (i+1),
                        truncate(wb.getExerciseName(), 25),
                        wb.getSets(),
                        wb.getReps(),
                        wb.getRestTime());
            }
        }
    }

    private String truncate(String str, int width) {
        if (str.length() > width) return str.substring(0, width - 3) + "...";
        return str;
    }

    // --- IMPLEMENTAZIONE INTERFACCIA WorkoutBuilderView ---

    @Override
    public void setListener(PlanManagerController controller) {
        this.listener = controller;
    }

    @Override
    public String getPlanName() { return this.planName; }

    @Override
    public void setPlanName(String name) { this.planName = name; }

    @Override
    public String getComment() { return this.planComment; }

    @Override
    public void setPlanComment(String comment) { this.planComment = comment; }

    @Override
    public void populateExerciseMenu(List<ExerciseBean> exercises) {
        // In CLI usiamo la ricerca, non il menu statico.
    }

    @Override
    public void updateExerciseTable(List<WorkoutExerciseBean> exercises) {
        this.addedExercises = exercises; // Aggiorna la lista locale
        // Non stampiamo nulla qui per non intasare la console, l'utente vedrà nel riepilogo
    }

    @Override
    public void updateExerciseList(List<WorkoutExerciseBean> exercises) {
        this.addedExercises = exercises;
    }

    @Override
    public List<WorkoutExerciseBean> getAddedExercises() {
        return this.addedExercises;
    }

    @Override
    public void updateTotalTime(String timeMessage) {
        System.out.println("[INFO] " + timeMessage);
    }

    // --- FIX FONDAMENTALE PER IL SALVATAGGIO ---
    @Override
    public WorkoutPlanBean getWorkoutPlanBean() {
        WorkoutPlanBean bean = new WorkoutPlanBean();
        bean.setName(this.planName);
        bean.setComment(this.planComment);
        // Restituisce una copia della lista esercizi per il salvataggio
        bean.setExerciseList(new ArrayList<>(this.addedExercises));
        return bean;
    }
    // -------------------------------------------

    @Override
    public void showSuccess(String message) {
        System.out.println("✅ SUCCESSO: " + message);
    }

    @Override
    public void showError(String message) {
        System.out.println("❌ ERRORE: " + message);
    }
}