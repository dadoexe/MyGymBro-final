package com.example.mygymbro.dao;

import java.util.logging.Logger;

public class DAOFactory {

    private static final Logger logger = Logger.getLogger(DAOFactory.class.getName());

    private static boolean isDemoMode = false;

    // CORREZIONE: Ora è PRIVATE (SonarCloud è felice)
    private static boolean confUseFileSystem = false;

    private DAOFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static void setDemoMode(boolean active) {
        isDemoMode = active;
        logger.info(() -> "DAOFactory: Modalità DEMO (RAM) impostata su " + active);
    }

    // NUOVO METODO: Usa questo per attivare il FileSystem da fuori!
    public static void setUseFileSystem(boolean active) {
        confUseFileSystem = active;
        logger.info(() -> "DAOFactory: Modalità FileSystem impostata su " + active);
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
            return null; // O MySQLPersonalTrainerDAO se esiste
        }
    }

    public static WorkoutPlanDAO getWorkoutPlanDAO() {
        // 1. Priorità alla DEMO MODE
        if (isDemoMode) {
            return new InMemoryWorkoutPlanDAO();
        }

        // 2. Se siamo in FULL MODE, controlliamo la flag privata
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