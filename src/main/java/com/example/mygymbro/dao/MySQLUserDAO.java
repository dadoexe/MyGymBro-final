package com.example.mygymbro.dao;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.*;
import java.sql.SQLException;
public class MySQLUserDAO implements UserDAO {


    @Override
    public User findByUsername(String username) {
        // 1. QUERY COMPLETA: Guarda sia in ATHLETE che in PERSONAL_TRAINER
        String query = "SELECT u.*, " +
                "a.weight, a.height, a.age, " +   // Dati Atleta
                "pt.cert_code " +                 // Dati Trainer
                "FROM user u " +
                "LEFT JOIN athlete a ON u.id = a.user_id " +
                "LEFT JOIN personal_trainer pt ON u.id = pt.user_id " + // <--- FONDAMENTALE
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
                // --- A. RECUPERO DATI COMUNI (Tabella User) ---
                int id = rs.getInt("id");
                String dbUsername = rs.getString("username");
                String dbPassword = rs.getString("password");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String email = rs.getString("email");

                // --- B. CONTROLLO DI TIPO ---
                String certCode = rs.getString("cert_code");

                if (certCode != null && !certCode.isEmpty()) {
                    // 1. Trovato codice certificazione -> È UN TRAINER!
                    user = new PersonalTrainer(id, dbUsername, dbPassword, nome, cognome, email, certCode);
                } else {
                    // 2. Altrimenti -> È UN ATLETA (Default)
                    float weight = rs.getFloat("weight");
                    float height = rs.getFloat("height");
                    int age = rs.getInt("age");
                    user = new Athlete(id, dbUsername, dbPassword, nome, age, email, cognome, weight, height);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore nel findByUsername: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DAOUtils.close(conn, stmt, rs);
        }

        return user;
    }


    @Override
    public void save(User user) throws SQLException{
        if (user == null) {
        throw new SQLException("impossibile salvare utente nullo");}
        //controllo il tipo a runtime e smisto la chiamata
        if(user instanceof Athlete){
            //faccio il cast e chiamo il metodo corrispondente
            saveAthlete((Athlete) user);
        }else if(user instanceof PersonalTrainer){
            //faccio il cast e chiamo il metodo corrispondente
            saveTrainer((PersonalTrainer) user);
        }else{//arrivati qua stiamo provando a salvare una classe sconosciuta
            throw new SQLException("tipo di utente non supportato "+user.getClass().getName());
            }
        }

    private void saveAthlete(Athlete athlete) throws SQLException{
        // Corrisponde ai campi in User.java
        String userQuery = "INSERT INTO user (username, password, name, cognome, email) VALUES (?, ?, ?, ?, ?)";

        String athleteQuery = "INSERT INTO athlete (user_id, weight, height, age) VALUES (?, ?, ?, ?)";
        String trainerQuery = "INSERT INTO personal_trainer (user_id, cert_code) VALUES (?, ?)";

        Connection connection = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtAthlete = null;
        ResultSet generatedKeys = null;
        try {
            connection = DBConnect.getConnection();
            connection.setAutoCommit(false);

            stmtUser = connection.prepareStatement(userQuery);
            stmtUser.setString(1, athlete.getUsername());
            stmtUser.setString(2, athlete.getPassword());
            stmtUser.setString(3, athlete.getName());
            stmtUser.setString(4, athlete.getCognome());
            stmtUser.setString(5, athlete.getEmail());
            stmtUser.executeUpdate();

            //recupero id generato
            generatedKeys = stmtUser.getGeneratedKeys();
            int newId = -1; //sentinel value
            if (generatedKeys.next()) {
                newId = generatedKeys.getInt(1);
                athlete.setId(newId); //aggiorno oggetto in memoria
            }else{throw new SQLException("Creazione fallita, nessun id ottenuto");}

            //ora salvo in Athlete
            stmtAthlete = connection.prepareStatement(athleteQuery);
            stmtAthlete.setInt(1, newId);
            stmtAthlete.setFloat(2, athlete.getWeight());
            stmtAthlete.setFloat(3, athlete.getHeight());
            stmtAthlete.setInt(4, athlete.getAge());
            stmtAthlete.executeUpdate();
            connection.commit(); //confermo
        }catch (SQLException e){
            if(connection!=null){connection.rollback();} //rollback in caso di errore
            throw e;
        }finally {
            DAOUtils.close(connection, stmtUser, generatedKeys);
            DAOUtils.closeStatement(stmtAthlete);
        }

    }

    private void saveTrainer(PersonalTrainer trainer) throws SQLException {
        String userQuery = "INSERT INTO user (username, password, name, cognome, email) VALUES (?, ?, ?, ?, ?)";
        String trainerQuery = "INSERT INTO personal_trainer (user_id, cert_code) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtTrainer = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            // --- STEP 1: Salvo in USER (Identico a sopra) ---
            stmtUser = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, trainer.getUsername());
            stmtUser.setString(2, trainer.getPassword());
            stmtUser.setString(3, trainer.getName());
            stmtUser.setString(4, trainer.getCognome());
            stmtUser.setString(5, trainer.getEmail());
            stmtUser.executeUpdate();

            generatedKeys = stmtUser.getGeneratedKeys();
            int newId = -1;
            if (generatedKeys.next()) {
                newId = generatedKeys.getInt(1);
                trainer.setId(newId);
            } else {
                throw new SQLException("Errore ID.");
            }

            // --- STEP 2: Salvo in PERSONAL_TRAINER ---
            stmtTrainer = conn.prepareStatement(trainerQuery);
            stmtTrainer.setInt(1, newId);                 // FK: user_id
            stmtTrainer.setString(2, trainer.getCertCode()); // Campo 'certCode' dallo screen
            stmtTrainer.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            DAOUtils.close(conn, stmtUser, generatedKeys);
            DAOUtils.closeStatement(stmtTrainer);
        }
    }
}
