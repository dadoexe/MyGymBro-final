package com.example.mygymbro.dao;

import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLUserDAO implements UserDAO {

    // 1. Logger per la gestione professionale degli errori (SonarCloud Compliance)
    private static final Logger LOGGER = Logger.getLogger(MySQLUserDAO.class.getName());

    @Override
    public User findByUsername(String username) {
        // 2. Esplicitiamo le colonne (Niente SELECT *)
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
            conn = DBConnect.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel findByUsername per l''utente: {0}", username);
            LOGGER.log(Level.SEVERE, "Dettaglio tecnico: ", e);
        } finally {
            DAOUtils.close(conn, stmt, rs);
        }
        return user;
    }

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
            athlete.setWeight(rs.getFloat("weight"));
            athlete.setHeight(rs.getFloat("height"));
            athlete.setAge(rs.getInt("age"));
            return athlete;
        }
    }

    @Override
    public void save(User user) throws SQLException {
        if (user == null) {
            throw new SQLException("Impossibile salvare utente nullo");
        }

        // 3. Pattern Matching con switch expression (Java 21+)
        switch (user) {
            case Athlete athlete -> saveAthlete(athlete);
            case PersonalTrainer trainer -> saveTrainer(trainer);
            case null, default -> throw new SQLException("Tipo di utente non supportato: " +
                    (user != null ? user.getClass().getName() : "null"));
        }
    }

    private void saveAthlete(Athlete athlete) throws SQLException {
        // 4. Usiamo 'nome' coerentemente con il DB
        String userQuery = "INSERT INTO user (username, password, nome, cognome, email) VALUES (?, ?, ?, ?, ?)";
        String athleteQuery = "INSERT INTO athlete (user_id, weight, height, age) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);

            // Necessario RETURN_GENERATED_KEYS per recuperare l'ID dell'utente appena creato
            try (PreparedStatement stmtUser = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {

                stmtUser.setString(1, athlete.getUsername());
                stmtUser.setString(2, athlete.getPassword());
                stmtUser.setString(3, athlete.getName());
                stmtUser.setString(4, athlete.getCognome());
                stmtUser.setString(5, athlete.getEmail());
                stmtUser.executeUpdate();

                int newId = getGeneratedId(stmtUser);
                athlete.setId(newId);

                try (PreparedStatement stmtAthlete = conn.prepareStatement(athleteQuery)) {
                    stmtAthlete.setInt(1, newId);
                    stmtAthlete.setFloat(2, athlete.getWeight());
                    stmtAthlete.setFloat(3, athlete.getHeight());
                    stmtAthlete.setInt(4, athlete.getAge());
                    stmtAthlete.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void saveTrainer(PersonalTrainer trainer) throws SQLException {
        String userQuery = "INSERT INTO user (username, password, nome, cognome, email) VALUES (?, ?, ?, ?, ?)";
        String trainerQuery = "INSERT INTO personal_trainer (user_id, cert_code) VALUES (?, ?)";

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtUser = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {

                stmtUser.setString(1, trainer.getUsername());
                stmtUser.setString(2, trainer.getPassword());
                stmtUser.setString(3, trainer.getName());
                stmtUser.setString(4, trainer.getCognome());
                stmtUser.setString(5, trainer.getEmail());
                stmtUser.executeUpdate();

                int newId = getGeneratedId(stmtUser);
                trainer.setId(newId);

                try (PreparedStatement stmtTrainer = conn.prepareStatement(trainerQuery)) {
                    stmtTrainer.setInt(1, newId);
                    stmtTrainer.setString(2, trainer.getCertCode());
                    stmtTrainer.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Helper per recuperare l'ID generato dal DB
    private int getGeneratedId(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Errore: Impossibile recuperare l'ID generato.");
        }
    }
}
