package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.*;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLWorkoutPlanDAO implements WorkoutPlanDAO {

    private static final Logger LOGGER = Logger.getLogger(MySQLWorkoutPlanDAO.class.getName());

    // Costanti SQL per evitare duplicazione
    private static final String DELETE_WORKOUT_EXERCISES_SQL = "DELETE FROM workout_exercise WHERE workout_plan_id = ?";
    private static final String INSERT_WORKOUT_PLAN_SQL = "INSERT INTO workout_plan (name, comment, creation_date, athlete_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_WORKOUT_PLAN_SQL = "UPDATE workout_plan SET name = ?, comment = ?, creation_date = ? WHERE id = ?";
    private static final String INSERT_WORKOUT_EXERCISE_SQL = "INSERT INTO workout_exercise (workout_plan_id, exercise_id, sets, reps, rest_time) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_PLANS_BY_ATHLETE_SQL = "SELECT id, name, comment, creation_date FROM workout_plan WHERE athlete_id = ? ORDER BY creation_date DESC";
    private static final String DELETE_WORKOUT_PLAN_SQL = "DELETE FROM workout_plan WHERE id = ?";

    @Override
    public void save(WorkoutPlan plan) throws DAOException {
        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            boolean isUpdate = (plan.getId() > 0);

            if (isUpdate) {
                updateWorkoutPlan(plan, conn);
            } else {
                insertWorkoutPlan(plan, conn);
            }

            saveWorkoutExercises(plan, conn, isUpdate);

            conn.commit();

        } catch (SQLException e) {
            rollbackConnection(conn);
            throw new DAOException("Errore durante il salvataggio della scheda", e);
        } finally {
            DAOUtils.closeConnection(conn);
        }
    }

    private void insertWorkoutPlan(WorkoutPlan plan, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_WORKOUT_PLAN_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, plan.getName());
            stmt.setString(2, plan.getComment());
            stmt.setDate(3, new java.sql.Date(plan.getCreationDate().getTime()));
            stmt.setInt(4, plan.getAthleteId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    plan.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    private void updateWorkoutPlan(WorkoutPlan plan, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_WORKOUT_PLAN_SQL)) {
            stmt.setString(1, plan.getName());
            stmt.setString(2, plan.getComment());
            stmt.setDate(3, new java.sql.Date(plan.getCreationDate().getTime()));
            stmt.setInt(4, plan.getId());
            stmt.executeUpdate();
        }
    }

    private void saveWorkoutExercises(WorkoutPlan plan, Connection conn, boolean isUpdate) throws SQLException {
        if (isUpdate) {
            deleteExistingExercises(plan.getId(), conn);
        }

        insertWorkoutExercises(plan, conn);
    }

    private void deleteExistingExercises(int planId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_WORKOUT_EXERCISES_SQL)) {
            stmt.setInt(1, planId);
            stmt.executeUpdate();
        }
    }

    private void insertWorkoutExercises(WorkoutPlan plan, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_WORKOUT_EXERCISE_SQL)) {
            for (WorkoutExercise we : plan.getExercises()) {
                int exerciseId = getOrInsertExercise(we.getExerciseDefinition(), conn);

                stmt.setInt(1, plan.getId());
                stmt.setInt(2, exerciseId);
                stmt.setInt(3, we.getSets());
                stmt.setInt(4, we.getReps());
                stmt.setInt(5, we.getRestTime());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

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

        return insertNewExercise(ex, conn);
    }

    private int insertNewExercise(Exercise ex, Connection conn) throws SQLException {
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
    public List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<WorkoutPlan> plans = new ArrayList<>();

        try {
            conn = DBConnect.getConnection();
            stmt = conn.prepareStatement(SELECT_PLANS_BY_ATHLETE_SQL);
            stmt.setInt(1, athlete.getId());
            rs = stmt.executeQuery();

            while (rs.next()) {
                WorkoutPlan plan = createWorkoutPlanFromResultSet(rs, athlete, conn);
                plans.add(plan);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero delle schede atleta", e);
        } finally {
            DAOUtils.close(conn, stmt, rs);
        }
        return plans;
    }

    private WorkoutPlan createWorkoutPlanFromResultSet(ResultSet rs, Athlete athlete, Connection conn) throws SQLException {
        int planId = rs.getInt("id");
        String name = rs.getString("name");
        String comment = rs.getString("comment");
        Date date = rs.getDate("creation_date");

        WorkoutPlan plan = new WorkoutPlan(planId, name, comment, date, athlete);

        List<WorkoutExercise> exercises = loadExercisesForPlan(planId, conn);
        for (WorkoutExercise ex : exercises) {
            plan.addExercise(ex);
        }

        return plan;
    }

    private List<WorkoutExercise> loadExercisesForPlan(int planId, Connection conn) throws SQLException {
        String query = "SELECT we.sets, we.reps, we.rest_time, we.exercise_id, " +
                "e.name, e.description, e.muscle_group " +
                "FROM workout_exercise we " +
                "JOIN exercise e ON we.exercise_id = e.id " +
                "WHERE we.workout_plan_id = ?";

        List<WorkoutExercise> exercises = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, planId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutExercise wExercise = createWorkoutExerciseFromResultSet(rs);
                    exercises.add(wExercise);
                }
            }
        }

        return exercises;
    }

    private WorkoutExercise createWorkoutExerciseFromResultSet(ResultSet rs) throws SQLException {
        Exercise definition = new Exercise(
                rs.getInt("exercise_id"),
                rs.getString("name"),
                rs.getString("description"),
                MuscleGroup.valueOf(rs.getString("muscle_group").toUpperCase())
        );

        return new WorkoutExercise(
                definition,
                rs.getInt("sets"),
                rs.getInt("reps"),
                rs.getInt("rest_time")
        );
    }

    @Override
    public void update(WorkoutPlan plan) throws DAOException {
        String updatePlanQuery = "UPDATE workout_plan SET name = ?, comment = ? WHERE id = ?";

        Connection conn = null;

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            updatePlanDetails(plan, conn, updatePlanQuery);
            deleteExistingExercises(plan.getId(), conn);
            insertUpdatedExercises(plan, conn);

            conn.commit();

        } catch (SQLException e) {
            rollbackConnection(conn);
            throw new DAOException("Errore durante l'aggiornamento della scheda", e);
        } finally {
            DAOUtils.closeConnection(conn);
        }
    }

    private void updatePlanDetails(WorkoutPlan plan, Connection conn, String updateQuery) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, plan.getName());
            stmt.setString(2, plan.getComment());
            stmt.setInt(3, plan.getId());
            stmt.executeUpdate();
        }
    }

    private void insertUpdatedExercises(WorkoutPlan plan, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_WORKOUT_EXERCISE_SQL)) {
            for (WorkoutExercise we : plan.getExercises()) {
                Exercise ex = we.getExercise();
                if (ex == null) {
                    ex = we.getExerciseDefinition();
                }

                int realExerciseId = getOrInsertExercise(ex, conn);

                stmt.setInt(1, plan.getId());
                stmt.setInt(2, realExerciseId);
                stmt.setInt(3, we.getSets());
                stmt.setInt(4, we.getReps());
                stmt.setInt(5, we.getRestTime());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public void delete(int planId) throws DAOException {
        Connection conn = null;

        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            deleteExistingExercises(planId, conn);
            int deletedRows = deleteWorkoutPlanById(planId, conn);

            if (deletedRows == 0) {
                LOGGER.log(Level.WARNING, "Nessuna scheda trovata con ID: {0}", planId);
            }

            conn.commit();

        } catch (SQLException e) {
            rollbackConnection(conn);
            throw new DAOException("Errore durante l'eliminazione della scheda", e);
        } finally {
            DAOUtils.closeConnection(conn);
        }
    }

    private int deleteWorkoutPlanById(int planId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_WORKOUT_PLAN_SQL)) {
            stmt.setInt(1, planId);
            return stmt.executeUpdate();
        }
    }

    private void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Errore durante il rollback", ex);
            }
        }
    }
}
