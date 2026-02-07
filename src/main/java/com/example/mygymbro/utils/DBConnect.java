package com.example.mygymbro.utils; // Assicurati che il package sia giusto

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    // DATI ESATTI CHE HAI USATO NEL TEST FUNZIONANTE
    private static final String URL = "jdbc:mysql://localhost:3306/mygymbro";
    private static final String USER = "root";
    private static final String PASS = "";  // <--- IMPORTANTE: Vuota per XAMPP!

    // Singleton instance (opzionale, ma consigliato)
    private static Connection connection = null;

    // Metodo per ottenere la connessione
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Caricamento esplicito del driver (per sicurezza)
                Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("CONNESSIONE DB STABILITA CON SUCCESSO!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("ERRORE DI CONNESSIONE NEL DAO:");
            e.printStackTrace(); // <--- Questo stamperà l'errore vero in console
            return null;
        }
        return connection;
    }
}
