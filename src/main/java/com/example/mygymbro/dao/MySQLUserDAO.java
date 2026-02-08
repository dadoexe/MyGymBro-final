package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLUserDAO implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(MySQLUserDAO.class.getName());

    // Codici errore MySQL
    private static final int MYSQL_DUPLICATE_KEY = 1062;
    private static final int MYSQL_FOREIGN_KEY_CONSTRAINT = 1452;
    private static final int MYSQL_CONNECTION_ERROR = 2002;
    private static final int MYSQL_ACCESS_DENIED = 1045;

    @Override
    public User findByUsername(String username) throws DAOException {
        String query = "SELECT u.id, u.username, u.password, u.nome, u.cognome, u.email, " +
                "a.weight, a.height, a.age, pt.cert_code " +
                "FROM user u " +
                "LEFT JOIN athlete a ON u.id = a.user_id " +
                "LEFT JOIN personal_trainer pt ON u.id = pt.user_id " +
                "WHERE u.username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getValidConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
                LOGGER.log(Level.FINE, "Utente trovato: {0} (ID: {1})",
                        new Object[]{username, user.getId()});
            }

        } catch (SQLException e) {
            handleSQLException(e, "ricerca utente per username");
        } finally {
            DAOUtils.close(conn, stmt, rs);
        }
        return user;
    }

    @Override
    public void save(User user) throws DAOException {
        if (user == null) {
            throw new DAOException("Impossibile salvare utente nullo", null);
        }

        // Pattern Matching con switch expression (Java 21+)
        switch (user) {
            case Athlete athlete -> saveAthlete(athlete);
            case PersonalTrainer trainer -> saveTrainer(trainer);
            case null, default -> throw new DAOException(
                    "Tipo di utente non supportato: " + (user != null ? user.getClass().getName() : "null"),
                    null
            );
        }
    }

    // ========== METODI DI SALVATAGGIO ==========

    private void saveAthlete(Athlete athlete) throws DAOException {
        String userQuery = "INSERT INTO user (username, password, nome, cognome, email) VALUES (?, ?, ?, ?, ?)";
        String athleteQuery = "INSERT INTO athlete (user_id, weight, height, age) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getValidConnection();
            conn.setAutoCommit(false);

            int newId = insertUserRecord(athlete, conn, userQuery);
            athlete.setId(newId);

            insertAthleteRecord(athlete, conn, athleteQuery);

            conn.commit();

            LOGGER.log(Level.INFO, "Atleta salvato con successo - Username: {0}, ID: {1}",
                    new Object[]{athlete.getUsername(), newId});

        } catch (SQLException e) {
            rollbackConnection(conn);
            handleSQLException(e, "salvataggio atleta");
        } finally {
            DAOUtils.closeConnection(conn);
        }
    }

    private void saveTrainer(PersonalTrainer trainer) throws DAOException {
        String userQuery = "INSERT INTO user (username, password, nome, cognome, email) VALUES (?, ?, ?, ?, ?)";
        String trainerQuery = "INSERT INTO personal_trainer (user_id, cert_code) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = getValidConnection();
            conn.setAutoCommit(false);

            int newId = insertUserRecord(trainer, conn, userQuery);
            trainer.setId(newId);

            insertTrainerRecord(trainer, conn, trainerQuery);

            conn.commit();

            LOGGER.log(Level.INFO, "Trainer salvato con successo - Username: {0}, ID: {1}",
                    new Object[]{trainer.getUsername(), newId});

        } catch (SQLException e) {
            rollbackConnection(conn);
            handleSQLException(e, "salvataggio trainer");
        } finally {
            DAOUtils.closeConnection(conn);
        }
    }

    private int insertUserRecord(User user, Connection conn, String userQuery) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getCognome());
            stmt.setString(5, user.getEmail());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento utente fallito, nessuna riga modificata");
            }

            return getGeneratedId(stmt);
        }
    }

    private void insertAthleteRecord(Athlete athlete, Connection conn, String athleteQuery) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(athleteQuery)) {
            stmt.setInt(1, athlete.getId());
            stmt.setFloat(2, athlete.getWeight());
            stmt.setFloat(3, athlete.getHeight());
            stmt.setInt(4, athlete.getAge());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento dati atleta fallito");
            }
        }
    }

    private void insertTrainerRecord(PersonalTrainer trainer, Connection conn, String trainerQuery) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(trainerQuery)) {
            stmt.setInt(1, trainer.getId());
            stmt.setString(2, trainer.getCertCode());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento dati trainer fallito");
            }
        }
    }

    // ========== MAPPING RESULTSET → OGGETTI ==========

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String dbUsername = rs.getString("username");
        String dbPassword = rs.getString("password");
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String email = rs.getString("email");
        String certCode = rs.getString("cert_code");

        if (certCode != null && !certCode.isEmpty()) {
            return new PersonalTrainer(id, dbUsername, dbPassword, nome, cognome, email, certCode);
        } else {
            Athlete athlete = new Athlete(id, dbUsername, dbPassword, nome, cognome, email);

            // Gestione sicura dei campi nullable
            float weight = rs.getFloat("weight");
            if (!rs.wasNull()) {
                athlete.setWeight(weight);
            }

            float height = rs.getFloat("height");
            if (!rs.wasNull()) {
                athlete.setHeight(height);
            }

            int age = rs.getInt("age");
            if (!rs.wasNull()) {
                athlete.setAge(age);
            }

            return athlete;
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
            case MYSQL_DUPLICATE_KEY:
                LOGGER.log(Level.WARNING, "Violazione chiave duplicata durante {0}", operation);
                throw new DAOException("Record duplicato nel database", e);

            case MYSQL_FOREIGN_KEY_CONSTRAINT:
                LOGGER.log(Level.WARNING, "Violazione vincolo chiave esterna durante {0}", operation);
                throw new DAOException("Riferimento a record inesistente", e);

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

    private void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.log(Level.INFO, "Rollback transazione completato");
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Errore durante rollback", ex);
            }
        }
    }

    private int getGeneratedId(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Impossibile recuperare l'ID generato dal database");
        }
    }
}
