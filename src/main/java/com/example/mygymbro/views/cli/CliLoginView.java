package com.example.mygymbro.views.cli;

import com.example.mygymbro.controller.LoginController;
import com.example.mygymbro.controller.SessionManager; // <--- IMPORTANTE
import com.example.mygymbro.views.LoginView;

import java.util.Scanner;

public class CliLoginView implements LoginView, CliView {

    private LoginController listener;
    private final Scanner scanner;

    private String username;
    private String password;

    public CliLoginView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        System.out.println("===============================");
        System.out.println("   BENVENUTO IN MYGYMBRO CLI   ");
        System.out.println("===============================");

        boolean keepRunning = true;

        while (keepRunning) {
            // Controllo preventivo: Se siamo già loggati, usciamo dal login!
            if (SessionManager.getInstance().getCurrentUser() != null) {
                keepRunning = false;
                break;
            }

            System.out.print("\nInserisci Username (o 'exit' per uscire): ");
            this.username = scanner.nextLine().trim();

            if (this.username.equalsIgnoreCase("exit")) {
                System.out.println("Arrivederci!");
                System.exit(0);
            }

            System.out.print("Inserisci Password: ");
            this.password = scanner.nextLine().trim();

            if (listener != null) {
                // Questo chiamerà loadHome() se ha successo
                listener.checkLogin();

                // FIX LOOP ZOMBIE:
                // Se dopo il checkLogin l'utente è stato settato in sessione,
                // significa che siamo entrati nella Dashboard e ne siamo appena usciti (o abbiamo cambiato vista).
                // Quindi questo loop di login deve morire.
                if (SessionManager.getInstance().getCurrentUser() != null) {
                    keepRunning = false;
                }
            }
        }
    }

    // --- IMPLEMENTAZIONE METODI INTERFACCIA ---
    @Override public void setListener(LoginController listener) { this.listener = listener; }
    @Override public String getUsername() { return this.username; }
    @Override public String getPassword() { return this.password; }

    @Override
    public void showSuccess(String message) {
        System.out.println(">> LOGIN OK: " + message);
    }

    @Override
    public void showError(String message) {
        System.out.println("!! ERRORE: " + message);
    }
}