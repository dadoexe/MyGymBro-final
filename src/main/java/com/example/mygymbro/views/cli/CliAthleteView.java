package com.example.mygymbro.views.cli;

import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.NavigationController;
import com.example.mygymbro.controller.SessionManager;
import com.example.mygymbro.views.AthleteView;

import java.util.List;
import java.util.Scanner;

public class CliAthleteView implements AthleteView, CliView {

    private NavigationController listener;
    private Scanner scanner;
    private List<WorkoutPlanBean> myPlansCache;

    public CliAthleteView() {
        this.scanner = new Scanner(System.in);
    }

    @Override public void showSuccess(String msg) { System.out.println("✅ " + msg); }
    @Override public void showError(String msg) { System.out.println("❌ " + msg); }

    @Override
    public void run() {
        // RIMOSSO IL BLOCCO 'FIX BUFFER' CHE CAUSAVA IL DOPPIO INVIO
        // Niente System.in.available(), niente hasNextLine().

        // Carichiamo i dati solo se non li abbiamo (evita ricaricamenti doppi se torni indietro)
        if (myPlansCache == null && listener != null) {
            listener.loadDashboardData();
        }

        boolean stay = true;
        while (stay) {
            // Controllo Sessione: se sloggato, esci subito
            if (SessionManager.getInstance().getCurrentUser() == null) {
                stay = false;
                break;
            }

            System.out.println("\n=== MENU PRINCIPALE ===");
            System.out.println("1. Crea Nuova Scheda");
            System.out.println("2. Gestisci le tue schede");
            System.out.println("0. Logout");
            System.out.print("Scelta > ");

            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) continue; // Ignora invii a vuoto senza bloccare

            switch (choice) {
                case "1":
                    stay = false; // Esci dal loop PRIMA di cambiare vista
                    if (listener != null) listener.loadWorkoutBuilder();
                    break;
                case "2":
                    // Se handleManagePlans ritorna true, significa che dobbiamo chiudere anche questo menu
                    if (handleManagePlans()) {
                        stay = false;
                    }
                    break;
                case "0":
                    stay = false;
                    if (listener != null) listener.logout();
                    break;
                default:
                    System.out.println("Comando non valido.");
            }
        }
    }

    // Ritorna TRUE se l'utente ha scelto un'azione che cambia vista (es. Live Session)
    private boolean handleManagePlans() {
        if (myPlansCache == null || myPlansCache.isEmpty()) {
            System.out.println("\n(Non hai ancora nessuna scheda salvata)");
            return false;
        }

        boolean managing = true;
        while (managing) {
            if (SessionManager.getInstance().getCurrentUser() == null) return true;

            System.out.println("\n--- SELEZIONA UNA SCHEDA ---");
            for (int i = 0; i < myPlansCache.size(); i++) {
                System.out.println((i + 1) + ". " + myPlansCache.get(i).getName());
            }
            System.out.println("0. Indietro");
            System.out.print("Numero > ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            try {
                int selection = Integer.parseInt(input);

                if (selection == 0) {
                    managing = false; // Torna al menu principale
                } else if (selection > 0 && selection <= myPlansCache.size()) {
                    WorkoutPlanBean selectedPlan = myPlansCache.get(selection - 1);

                    // Se askActionForPlan ritorna TRUE, usciamo da tutto per cambiare vista
                    if (askActionForPlan(selectedPlan)) {
                        return true;
                    }
                } else {
                    System.out.println("Numero non valido.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }
        return false;
    }

    // Ritorna TRUE se cambiamo vista
    private boolean askActionForPlan(WorkoutPlanBean plan) {
        System.out.println("\nScheda: " + plan.getName());
        System.out.println("1. Modifica");
        System.out.println("2. Elimina");
        System.out.println("3. AVVIA LIVE SESSION");
        System.out.println("0. Indietro");
        System.out.print("Azione > ");

        String action = scanner.nextLine().trim();
        switch (action) {
            case "1":
                if (listener != null) listener.modifyPlan(plan);
                return true; // CAMBIO VISTA
            case "2":
                System.out.print("Sicuro? (si/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
                    if (listener != null) listener.deletePlan(plan);
                }
                return false; // RESTA QUI
            case "3":
                System.out.println(">>> Avvio allenamento...");
                if (listener != null) listener.startLiveSession(plan);
                // Quando la live session finisce, torneremo qui.
                // Siccome la LiveSession crea la sua UI e poi la distrugge,
                // possiamo decidere se tornare al menu principale (true) o restare qui (false).
                // Per pulizia, torniamo al menu principale:
                return true;
            default:
                return false;
        }
    }

    // ... Interface methods ...
    @Override public void setListener(NavigationController l) { this.listener = l; }
    @Override public void updateWelcomeMessage(String m) {}
    @Override public void updateWorkoutList(List<WorkoutPlanBean> l) { this.myPlansCache = l; }
}