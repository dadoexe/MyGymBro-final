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

    @Override
    public void showSuccess(String msg) {
        System.out.println("✅ " + msg);
    }

    @Override
    public void showError(String msg) {
        System.out.println("❌ " + msg);
    }

    @Override
    public void run() {
        // Carichiamo i dati solo se non li abbiamo
        if (myPlansCache == null && listener != null) {
            listener.loadDashboardData();
        }

        while (isSessionActive()) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) {
                continue;
            }

            if (!processMainMenuChoice(choice)) {
                break; // Esce dal loop se l'utente sceglie logout o cambio vista
            }
        }
    }

    private boolean isSessionActive() {
        return SessionManager.getInstance().getCurrentUser() != null;
    }

    private void printMainMenu() {
        System.out.println("\n=== MENU PRINCIPALE ===");
        System.out.println("1. Crea Nuova Scheda");
        System.out.println("2. Gestisci le tue schede");
        System.out.println("0. Logout");
        System.out.print("Scelta > ");
    }

    private boolean processMainMenuChoice(String choice) {
        switch (choice) {
            case "1":
                if (listener != null) {
                    listener.loadWorkoutBuilder();
                }
                return false; // Cambio vista
            case "2":
                // Se ritorna true, dobbiamo uscire anche da questo menu (cambio vista)
                return !handleManagePlans();
            case "0":
                if (listener != null) {
                    listener.logout();
                }
                return false;
            default:
                System.out.println("Comando non valido.");
                return true; // Resta nel menu
        }
    }

    // Ritorna TRUE se l'utente ha scelto un'azione che cambia vista (es. Live Session)
    private boolean handleManagePlans() {
        if (myPlansCache == null || myPlansCache.isEmpty()) {
            System.out.println("\n(Non hai ancora nessuna scheda salvata)");
            return false;
        }

        while (isSessionActive()) {
            printPlanList();
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            int selection = parseSelection(input);
            if (selection == 0) {
                return false; // Torna indietro
            }

            if (selection > 0 && selection <= myPlansCache.size()) {
                WorkoutPlanBean selectedPlan = myPlansCache.get(selection - 1);
                // Se askActionForPlan ritorna TRUE, usciamo da tutto
                if (askActionForPlan(selectedPlan)) {
                    return true;
                }
            } else if (selection != -1) {
                System.out.println("Numero non valido.");
            }
        }
        return true; // Sessione terminata
    }

    private void printPlanList() {
        System.out.println("\n--- SELEZIONA UNA SCHEDA ---");
        for (int i = 0; i < myPlansCache.size(); i++) {
            System.out.println((i + 1) + ". " + myPlansCache.get(i).getName());
        }
        System.out.println("0. Indietro");
        System.out.print("Numero > ");
    }

    // Metodo helper per ridurre la complessità del try-catch
    private int parseSelection(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException _) {
            System.out.println("Inserisci un numero valido.");
            return -1; // Codice errore interno
        }
    }

    private boolean askActionForPlan(WorkoutPlanBean plan) {
        System.out.println("\nScheda: " + plan.getName());
        System.out.println("1. Modifica");
        System.out.println("2. Elimina");
        System.out.println("3. AVVIA LIVE SESSION");
        System.out.println("0. Indietro");
        System.out.print("Azione > ");

        String action = scanner.nextLine().trim();
        return switch (action) {
            case "1" -> {
                if (listener != null) {
                    listener.modifyPlan(plan);
                }
                yield true;
            }
            case "2" -> {
                deletePlanFlow(plan);
                yield false;
            }
            case "3" -> {
                System.out.println(">>> Avvio allenamento...");
                if (listener != null) {
                    listener.startLiveSession(plan);
                }
                yield true;
            }
            default -> false;
        };
    }

    private void deletePlanFlow(WorkoutPlanBean plan) {
        System.out.print("Sicuro? (si/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("si")) {
            if (listener != null) {
                listener.deletePlan(plan);
            }
        }
    }

    // ... Interface methods ...
    @Override
    public void setListener(NavigationController controller) {
        this.listener = controller;
    }

    @Override
    public void updateWelcomeMessage(String message) {
        // Metodo vuoto intenzionale: In CLI non mostriamo il messaggio di benvenuto dinamico
    }

    @Override
    public void updateWorkoutList(List<WorkoutPlanBean> plans) {
        this.myPlansCache = plans;
    }
}