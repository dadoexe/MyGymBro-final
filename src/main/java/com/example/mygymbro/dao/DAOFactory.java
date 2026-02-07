package com.example.mygymbro.dao;

import java.util.logging.Logger;

public class DAOFactory {

    private static final Logger logger = Logger.getLogger(DAOFactory.class.getName());

    private static boolean isDemoMode = false;

    // CORREZIONE 1: Rinominata in camelCase perché non è 'final'
    public static boolean confUseFileSystem = false;

    // Costruttore privato per nascondere quello pubblico implicito (Regola Sonar)
    private DAOFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static void setDemoMode(boolean active) {
        isDemoMode = active;
        // CORREZIONE 2: Logger invece di System.out
        logger.info(() -> "DAOFactory: Modalità DEMO (RAM) impostata su " + active);
    }

    public static UserDAO getUserDAO() {
        if (isDemoMode) {
            return new InMemoryUserDAO();
        } else {
            return new MySQLUserDAO();
        }
    }

    public static PersonalTrainerDAO getPersonalTrainerDAO() {
        if (isDemoMode) {
            return new InMemoryPersonalTrainerDAO();
        } else {
            // Se non hai implementato MySQLPersonalTrainerDAO, ritorna null o gestiscilo
            return null;
        }
    }

    public static WorkoutPlanDAO getWorkoutPlanDAO() {
        // 1. Priorità alla DEMO MODE
        if (isDemoMode) {
            return new InMemoryWorkoutPlanDAO();
        }

        // 2. Se siamo in FULL MODE, scegliamo la persistenza
        if (confUseFileSystem) {
            logger.info("DAOFactory: Utilizzo FileSystemDAO");
            return new FileSystemWorkoutPlanDAO();
        } else {
            logger.info("DAOFactory: Utilizzo MySQLDAO");
            return new MySQLWorkoutPlanDAO();
        }
    }

    public static ExerciseDAO getExerciseDAO() {
        return new RestApiExerciseDAO();
    }
}