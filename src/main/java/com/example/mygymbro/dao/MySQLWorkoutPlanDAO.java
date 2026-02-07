package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.*;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLWorkoutPlanDAO implements WorkoutPlanDAO {

    @Override
    public void save(WorkoutPlan plan) throws DAOException {
        Connection conn = null;
        PreparedStatement stmtPlan = null;
        PreparedStatement stmtLink = null;
        ResultSet generatedKeys = null;

        String insertSQL = "INSERT INTO workout_plan (name, comment, creation_date, athlete_id) VALUES (?, ?, ?, ?)";
        String updateSQL = "UPDATE workout_plan SET name = ?, comment = ?, creation_date = ? WHERE id = ?";
        String deleteExercisesSQL = "DELETE FROM workout_exercise WHERE workout_plan_id = ?";
        String insertLinkSQL = "INSERT INTO workout_exercise (workout_plan_id, exercise_id, sets, reps, rest_time) VALUES (?, ?, ?, ?, ?)";

        boolean isUpdate = (plan.getId() > 0);

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false); // Transazione ON

            // --- A. SALVIAMO O AGGIORNIAMO LA SCHEDA (PADRE) ---
            if (isUpdate) {
                stmtPlan = conn.prepareStatement(updateSQL);
                stmtPlan.setString(1, plan.getName());
                stmtPlan.setString(2, plan.getComment());
                stmtPlan.setDate(3, new java.sql.Date(plan.getCreationDate().getTime()));
                stmtPlan.setInt(4, plan.getId());
                stmtPlan.executeUpdate();
            } else {
                stmtPlan = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
                stmtPlan.setString(1, plan.getName());
                stmtPlan.setString(2, plan.getComment());
                stmtPlan.setDate(3, new java.sql.Date(plan.getCreationDate().getTime()));
                stmtPlan.setInt(4, plan.getAthleteId());
                stmtPlan.executeUpdate();

                generatedKeys = stmtPlan.getGeneratedKeys();
                if (generatedKeys.next()) {
                    plan.setId(generatedKeys.getInt(1));
                }
            }

            // --- B. GESTIONE ESERCIZI (FIGLI) ---
            if (isUpdate) {
                try (PreparedStatement delStmt = conn.prepareStatement(deleteExercisesSQL)) {
                    delStmt.setInt(1, plan.getId());
                    delStmt.executeUpdate();
                }
            }

            stmtLink = conn.prepareStatement(insertLinkSQL);
            for (WorkoutExercise we : plan.getExercises()) {
                int localExerciseId = getOrInsertExercise(we.getExerciseDefinition(), conn);

                stmtLink.setInt(1, plan.getId());
                stmtLink.setInt(2, localExerciseId);
                stmtLink.setInt(3, we.getSets());
                stmtLink.setInt(4, we.getReps());
                stmtLink.setInt(5, we.getRestTime());
                stmtLink.addBatch();
            }
            stmtLink.executeBatch();

            conn.commit();

        } catch (SQLException e) {
            // FIX: Gestione rollback sicura e wrap in DAOException
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new DAOException("Errore durante il salvataggio della scheda", e);
        } finally {
            if (generatedKeys != null) DAOUtils.closeResultSet(generatedKeys);
            if (stmtPlan != null) DAOUtils.closeStatement(stmtPlan);
            if (stmtLink != null) DAOUtils.closeStatement(stmtLink);
            if (conn != null) DAOUtils.closeConnection(conn);
        }
    }

    // Metodo helper privato: può lanciare SQLException perché è chiamato dentro un try-catch del metodo save/update
    private int getOrInsertExercise(Exercise ex, Connection conn) throws SQLException {
        String searchSQL = "SELECT id FROM exercise WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(searchSQL)) {
            stmt.setString(1, ex.getName());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSQL = "INSERT INTO exercise (name, description, muscle_group) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ex.getName());
            stmt.setString(2, ex.getDescription());
            String muscle = (ex.getMuscleGroup() != null) ? ex.getMuscleGroup().name() : "CHEST";
            stmt.setString(3, muscle);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Impossibile salvare l'esercizio locale: " + ex.getName());
    }


    @Override
    public List<WorkoutPlan> findByAthlete (Athlete athlete) throws DAOException {
        String query = "SELECT * FROM workout_plan WHERE Athlete_id = ? ORDER BY creation_date DESC";

        Connection conn= null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<WorkoutPlan> Plans = new ArrayList<>();

        try {
            conn = DBConnect.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, athlete.getId());
            rs = stmt.executeQuery();
            while(rs.next()){
                int planID = rs.getInt("id");
                String name = rs.getString("name");
                String comment = rs.getString("comment");
                Date date = rs.getDate("creation_date");

                WorkoutPlan plan = new WorkoutPlan(planID, name, comment, date, athlete);

                List<WorkoutExercise> exercises = loadExercisesForPlan(planID, conn);
                for(WorkoutExercise ex : exercises){
                    plan.addExercise(ex);
                }
                Plans.add(plan);
            }
        } catch (SQLException e) {
            // FIX: Wrap in DAOException
            throw new DAOException("Errore nel recupero delle schede atleta", e);
        } finally {
            DAOUtils.close(conn, stmt, rs);
        }
        return Plans;
    }

    private List<WorkoutExercise> loadExercisesForPlan(int planId, Connection conn) throws SQLException {
        String query = "SELECT we.*, e.name, e.description, e.muscle_group " +
                "FROM workout_exercise we " +
                "JOIN exercise e ON we.exercise_id = e.id " +
                "WHERE we.workout_plan_id = ?";

        List<WorkoutExercise> exercises = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, planId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Exercise definition = new Exercise(
                        rs.getInt("exercise_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        MuscleGroup.valueOf(rs.getString("muscle_group").toUpperCase())
                );

                WorkoutExercise wExercise = new WorkoutExercise(
                        definition,
                        rs.getInt("sets"),
                        rs.getInt("reps"),
                        rs.getInt("rest_time")
                );

                exercises.add(wExercise);
            }
        } finally {
            DAOUtils.closeStatement(stmt);
            DAOUtils.closeResultSet(rs);
        }
        return exercises;
    }

    @Override
    public void update(WorkoutPlan plan) throws DAOException {
        String updatePlanQuery = "UPDATE workout_plan SET name = ?, comment = ? WHERE id = ?";
        String deleteExercisesQuery = "DELETE FROM workout_exercise WHERE workout_plan_id = ?";
        String insertExerciseQuery = "INSERT INTO workout_exercise (workout_plan_id, exercise_id, sets, reps, rest_time) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psPlan = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            // 1. Aggiorna nome e descrizione
            psPlan = conn.prepareStatement(updatePlanQuery);
            psPlan.setString(1, plan.getName());
            psPlan.setString(2, plan.getComment());
            psPlan.setInt(3, plan.getId());
            psPlan.executeUpdate();

            // 2. Rimuovi vecchi esercizi
            psDelete = conn.prepareStatement(deleteExercisesQuery);
            psDelete.setInt(1, plan.getId());
            psDelete.executeUpdate();

            // 3. Inserisci nuovi esercizi
            psInsert = conn.prepareStatement(insertExerciseQuery);

            for (WorkoutExercise we : plan.getExercises()) {
                Exercise ex = we.getExercise();
                if (ex == null) ex = we.getExerciseDefinition();

                int realExerciseId = getOrInsertExercise(ex, conn);

                psInsert.setInt(1, plan.getId());
                psInsert.setInt(2, realExerciseId);
                psInsert.setInt(3, we.getSets());
                psInsert.setInt(4, we.getReps());
                psInsert.setInt(5, we.getRestTime());
                psInsert.addBatch();
            }
            psInsert.executeBatch();

            conn.commit();

        } catch (SQLException e) {
            // FIX: Rollback e Wrap exception
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new DAOException("Errore durante l'aggiornamento della scheda", e);
        } finally {
            DAOUtils.closeStatement(psPlan);
            DAOUtils.closeStatement(psDelete);
            DAOUtils.closeStatement(psInsert);
            DAOUtils.closeConnection(conn);
        }
    }

    @Override
    public void delete(int planId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmtDeleteExercise = null;
        PreparedStatement stmtDeletePlan = null;

        String deleteExerciseQuery = "DELETE FROM workout_exercise WHERE workout_plan_id = ?";
        String deleteWorkoutPlanQuery = "DELETE FROM workout_plan WHERE id = ?";

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            // 1. Cancellazione figli
            stmtDeleteExercise = conn.prepareStatement(deleteExerciseQuery);
            stmtDeleteExercise.setInt(1, planId);
            stmtDeleteExercise.executeUpdate();

            // 2. Cancellazione padre
            stmtDeletePlan = conn.prepareStatement(deleteWorkoutPlanQuery);
            stmtDeletePlan.setInt(1, planId);
            int rows = stmtDeletePlan.executeUpdate();

            if (rows == 0) {
                System.out.println("Nessuna scheda trovata con ID: " + planId);
            }

            conn.commit();

        } catch (SQLException e) {
            // FIX: Rollback e Wrap exception
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new DAOException("Errore durante l'eliminazione della scheda", e);
        } finally {
            DAOUtils.closeStatement(stmtDeleteExercise);
            DAOUtils.closeStatement(stmtDeletePlan);
            DAOUtils.closeConnection(conn);
        }
    }
}
