package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Exercise;
import com.example.mygymbro.model.MuscleGroup;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLExerciseDAO implements ExerciseDAO {

    private static final Logger LOGGER = Logger.getLogger(MySQLExerciseDAO.class.getName());

    // Codici errore MySQL
    private static final int MYSQL_CONNECTION_ERROR = 2002;
    private static final int MYSQL_ACCESS_DENIED = 1045;

    @Override
    public Exercise findByName(String name) throws DAOException {
        String query = "SELECT id, name, description, muscle_group FROM exercise WHERE name = ?";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Exercise exercise = null;

        try {
            conn = getValidConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, name);
            rs = statement.executeQuery();

            if (rs.next()) {
                exercise = mapRowToExercise(rs);
                LOGGER.log(Level.FINE, "Esercizio trovato: {0} (ID: {1})",
                        new Object[]{name, exercise.getId()});
            } else {
                LOGGER.log(Level.FINE, "Nessun esercizio trovato con nome: {0}", name);
            }

        } catch (SQLException e) {
            handleSQLException(e, "ricerca esercizio per nome");
        } finally {
            DAOUtils.close(conn, statement, rs);
        }
        return exercise;
    }

    @Override
    public List<Exercise> findAll() throws DAOException {
        String query = "SELECT id, name, description, muscle_group FROM exercise ORDER BY name ASC";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Exercise> exercises = new ArrayList<>();

        try {
            conn = getValidConnection();
            statement = conn.prepareStatement(query);
            rs = statement.executeQuery();

            while (rs.next()) {
                exercises.add(mapRowToExercise(rs));
            }

            LOGGER.log(Level.FINE, "Caricati {0} esercizi dal database", exercises.size());

        } catch (SQLException e) {
            handleSQLException(e, "caricamento elenco esercizi");
        } finally {
            DAOUtils.close(conn, statement, rs);
        }
        return exercises;
    }

    @Override
    public List<Exercise> search(String keyword) throws DAOException {
        if (keyword == null || keyword.trim().isEmpty()) {
            LOGGER.log(Level.FINE, "Ricerca con keyword vuota, ritorno lista vuota");
            return new ArrayList<>();
        }

        String query = "SELECT id, name, description, muscle_group FROM exercise " +
                "WHERE name LIKE ? OR description LIKE ? ORDER BY name ASC";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Exercise> exercises = new ArrayList<>();

        try {
            conn = getValidConnection();
            statement = conn.prepareStatement(query);

            String searchPattern = "%" + keyword + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);

            rs = statement.executeQuery();

            while (rs.next()) {
                exercises.add(mapRowToExercise(rs));
            }

            LOGGER.log(Level.FINE, "Ricerca '{0}': trovati {1} esercizi",
                    new Object[]{keyword, exercises.size()});

        } catch (SQLException e) {
            handleSQLException(e, "ricerca esercizi con keyword: " + keyword);
        } finally {
            DAOUtils.close(conn, statement, rs);
        }
        return exercises;
    }

    // ========== MAPPING RESULTSET → OGGETTI ==========

    private Exercise mapRowToExercise(ResultSet rs) throws SQLException {
        try {
            return new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    MuscleGroup.valueOf(rs.getString("muscle_group").toUpperCase())
            );
        } catch (IllegalArgumentException e) {
            // Gestione caso in cui muscle_group nel DB non corrisponde a nessun valore enum
            LOGGER.log(Level.WARNING, "Valore muscle_group non valido nel database per esercizio ID: {0}",
                    rs.getInt("id"));
            // Usa un valore di default
            return new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    MuscleGroup.CHEST  // Default fallback
            );
        }
    }

    // ========== GESTIONE CONNESSIONE E ERRORI ==========

    private Connection getValidConnection() throws DAOException {
        Connection conn = DBConnect.getConnection();
        if (conn == null) {
            throw new DAOException("Impossibile ottenere connessione al database", null);
        }
        return conn;
    }

    private void handleSQLException(SQLException e, String operation) throws DAOException {
        String baseMessage = "Errore database durante " + operation;

        // 🎯 GESTIONE SPECIFICA PER CODICE ERRORE SQL
        switch (e.getErrorCode()) {
            case MYSQL_CONNECTION_ERROR:
                LOGGER.log(Level.SEVERE, "Errore di connessione durante {0}", operation);
                throw new DAOException("Database non raggiungibile", e);

            case MYSQL_ACCESS_DENIED:
                LOGGER.log(Level.SEVERE, "Accesso negato durante {0}", operation);
                throw new DAOException("Credenziali database non valide", e);

            default:
                LOGGER.log(Level.SEVERE, "{0} - Codice: {1}, SQLState: {2}",
                        new Object[]{baseMessage, e.getErrorCode(), e.getSQLState()});
                throw new DAOException(baseMessage + ": " + e.getMessage(), e);
        }
    }
}
