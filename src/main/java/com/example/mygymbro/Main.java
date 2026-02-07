package com.example.mygymbro;

import com.example.mygymbro.controller.ApplicationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        // Percorso assoluto che corrisponde alla tua struttura cartelle
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/launcher.fxml"));

        // Controllo di sicurezza (Opzionale ma utile per debug)
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("ERRORE CRITICO: Non trovo il file launcher.fxml! Controlla il percorso in resources.");
        }

        Scene scene = new Scene(fxmlLoader.load(), 600, 400); // Aumenta un po' le dimensioni
        stage.setTitle("MyGymBro Launcher");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Controlliamo se l'utente vuole la modalità CLI (Console)
        // Puoi testarlo scrivendo "--cli" negli argomenti di avvio di IntelliJ
        boolean wantCli = false;

        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            wantCli = true;
        }

        // --- INTERRUTTORE DI AVVIO ---
        if (wantCli) {
            // 1. MODALITÀ TESTUALE (CLI)
            System.out.println(">>> AVVIO MYGYMBRO IN MODALITÀ CONSOLE (CLI) <<<");

            ApplicationController app = ApplicationController.getInstance();

            // Configuriamo in modalità CLI (False) e Stage null
            app.configure(false, null);

            // Avviamo (qui partirà il loop della console e il programma si bloccherà qui finché non esci)
            app.start();

        } else {
            // 2. MODALITÀ GRAFICA (DEFAULT)
            // Lancia JavaFX, che chiamerà il metodo start(Stage) qui sopra
            launch(args);
        }
    }
}