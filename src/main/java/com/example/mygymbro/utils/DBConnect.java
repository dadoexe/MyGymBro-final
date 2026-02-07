package com.example.mygymbro.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnect {

    private static final Logger logger = Logger.getLogger(DBConnect.class.getName());

    private static final String URL = "jdbc:mysql://localhost:3306/mygymbro";

    // MODIFICA QUI: Prendiamo le credenziali dalle variabili d'ambiente
    private static final String USER = System.getenv("DB_USER");
    private static final String PASS = System.getenv("DB_PASSWORD");

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Controllo di sicurezza per evitare NullPointerException se le variabili non sono settate
                String dbUser = (USER != null) ? USER : "root";
                String dbPass = (PASS != null) ? PASS : "";

                connection = DriverManager.getConnection(URL, dbUser, dbPass);
                logger.log(Level.INFO, "Connessione DB stabilita con successo!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Errore di connessione nel DAO", e);
            return null;
        }
        return connection;
    }
}