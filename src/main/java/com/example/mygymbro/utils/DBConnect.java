package com.example.mygymbro.utils; // Assicurati che il package sia giusto

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnect {

    private static final Logger logger = Logger.getLogger(DBConnect.class.getName());

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
                logger.log(Level.INFO, "Connessione DB stabilita con successo!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            // 4. USA IL LOGGER ANCHE PER GLI ERRORI (Level.SEVERE)
            // Vecchio: System.err.println("ERRORE...");
            // Vecchio: e.printStackTrace();
            logger.log(Level.SEVERE, "Errore di connessione nel DAO", e);
            return null; // O potresti voler rilanciare un'eccezione personalizzata
        }
        return connection;
    }
}
