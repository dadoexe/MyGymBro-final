package com.example.mygymbro.views.cli;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.TrainerController;
import com.example.mygymbro.views.TrainerView;

import java.util.List;
import java.util.Scanner;

public class CliTrainerView implements TrainerView, CliView {

    private TrainerController listener;
    private final Scanner scanner;

    private List<AthleteBean> cachedAthletes;
    private List<WorkoutPlanBean> cachedPlans;

    private AthleteBean selectedAthlete;
    private WorkoutPlanBean selectedPlan;

    public CliTrainerView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        boolean running = true;

        // Carica i dati iniziali (ma non stampa la lista completa subito, solo header)
        if (listener != null) listener.loadDashboardData();

        while (running) {
            System.out.println("\n--- DASHBOARD TRAINER ---");
            System.out.println("Cliente Attivo: " + (selectedAthlete != null ? "👤 " + selectedAthlete.getUsername().toUpperCase() : "❌ NESSUNO"));
            System.out.println("-------------------------");

            // MENU OTTIMIZZATO
            System.out.println("1. Seleziona/Cambia Cliente"); // Accorpa visualizzazione e selezione
            System.out.println("2. Visualizza Schede Cliente");
            System.out.println("3. Assegna Nuova Scheda");
            System.out.println("4. Modifica Scheda Esistente");
            System.out.println("0. Logout");
            System.out.print("Scelta > ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleSelectAthlete();
                    break;
                case "2":
                    if (checkSelection()) {
                        listener.loadPlansForAthlete(selectedAthlete);
                    }
                    break;
                case "3":
                    if (checkSelection()) {
                        // Quando torneremo qui, la selezione sarà persa SE non la passiamo
                        // (vedi modifiche successive)
                        listener.createNewPlan();
                        running = false; // Usciamo dal loop perché ApplicationController cambierà view
                    }
                    break;
                case "4":
                    handleModifyPlan();
                    break;
                case "0":
                    running = false;
                    listener.logout();
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    // --- LOGICA INTERNA ---

    private void handleSelectAthlete() {
        // Mostriamo la lista QUI, solo quando serve selezionare
        if (cachedAthletes == null || cachedAthletes.isEmpty()) {
            System.out.println("⚠️ Nessun cliente disponibile.");
            return;
        }

        System.out.println("\n--- SELEZIONA CLIENTE ---");
        for (int i = 0; i < cachedAthletes.size(); i++) {
            System.out.println((i + 1) + ". " + cachedAthletes.get(i).getUsername() +
                    " (" + cachedAthletes.get(i).getNome() + " " + cachedAthletes.get(i).getCognome() + ")");
        }
        System.out.println("0. Annulla");
        System.out.print("Numero > ");

        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index == -1) return; // Annulla

            if (index >= 0 && index < cachedAthletes.size()) {
                this.selectedAthlete = cachedAthletes.get(index);
                System.out.println("✅ Cliente attivo: " + selectedAthlete.getUsername());
                // Carichiamo subito le schede in background
                listener.loadPlansForAthlete(selectedAthlete);
            } else {
                System.out.println("Indice non valido.");
            }
        } catch (NumberFormatException ignored) { // <--- CAMBIATO DA 'e' A 'ignored'
            System.out.println("Inserisci un numero.");
        }
    }

    private void handleModifyPlan() {
        if (!checkSelection()) return;

        if (cachedPlans == null || cachedPlans.isEmpty()) {
            System.out.println("⚠️ Questo cliente non ha schede da modificare.");
            return;
        }

        System.out.println("\n--- MODIFICA SCHEDA ---");
        for (int i = 0; i < cachedPlans.size(); i++) {
            System.out.println((i + 1) + ". " + cachedPlans.get(i).getName());
        }
        System.out.println("0. Annulla");
        System.out.print("Numero > ");

        try {
            String line = scanner.nextLine();
            // Aggiungi un controllo di sicurezza per input vuoti
            if (line.trim().isEmpty()) return;

            int index = Integer.parseInt(line) - 1;
            if (index == -1) return;

            if (index >= 0 && index < cachedPlans.size()) {
                this.selectedPlan = cachedPlans.get(index);
                if (listener != null) listener.modifySelectedPlan();
            } else {
                System.out.println("Indice non valido.");
            }
        } catch (NumberFormatException ignored) { // <--- CAMBIATO DA 'e' A 'ignored'
            System.out.println("Inserisci un numero.");
        }
    }

    private boolean checkSelection() {
        if (selectedAthlete == null) {
            System.out.println("⚠️ DEVI PRIMA SELEZIONARE UN CLIENTE (Opzione 1)");
            return false;
        }
        return true;
    }

    // --- IMPLEMENTAZIONE INTERFACCIA ---
    @Override public void setListener(TrainerController controller) { this.listener = controller; }

    // Qui salviamo la lista ma NON la stampiamo (la stampiamo solo su richiesta nel handleSelect)
    @Override public void showAthletesList(List<AthleteBean> athletes) { this.cachedAthletes = athletes; }

    @Override
    public void showAthletePlans(List<WorkoutPlanBean> plans) {
        this.cachedPlans = plans;

        // Header
        System.out.println("\n--- SCHEDE DI " + (selectedAthlete != null ? selectedAthlete.getUsername() : "???") + " ---");

        // Feedback sul conteggio
        if (plans == null || plans.isEmpty()) {
            System.out.println("(Nessuna scheda assegnata)");
        } else {
            System.out.println(">> Trovate " + plans.size() + " schede:");

            // --- ECCO IL PEZZO CHE MANCAVA ---
            for (int i = 0; i < plans.size(); i++) {
                WorkoutPlanBean p = plans.get(i);
                System.out.println("   " + (i + 1) + ". " + p.getName() + " (" + p.getComment() + ")");
            }
            // ---------------------------------
        }
    }

    @Override public void updateWelcomeMessage(String msg) { /* Opzionale stampa benvenuto */ }
    @Override public AthleteBean getSelectedAthlete() { return this.selectedAthlete; }
    @Override public WorkoutPlanBean getSelectedPlan() { return this.selectedPlan; }

    // NUOVO METODO IMPLEMENTATO
    @Override public void setSelectedAthlete(AthleteBean athlete) {
        this.selectedAthlete = athlete;
        // Se ripristiniamo un atleta, ricarichiamo anche le sue schede
        if (listener != null && athlete != null) {
            listener.loadPlansForAthlete(athlete);
        }
    }

@Override
    public void showSuccess(String msg) {
        System.out.println("✅ SUCCESSO: " + msg);
    }

    @Override
    public void showError(String msg) {
        System.out.println("❌ ERRORE: " + msg);
    }

}