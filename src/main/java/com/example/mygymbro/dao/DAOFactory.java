package com.example.mygymbro.dao;


public class DAOFactory {

    private static boolean isDemoMode = false;

    // --- NUOVA CONFIGURAZIONE ---
    // Imposta questo a TRUE per testare il salvataggio su FILE per l'esame.
    // Imposta a FALSE per usare MySQL.
    public static boolean CONF_USE_FILESYSTEM = false;

    public static void setDemoMode(boolean active) {
        isDemoMode = active;
        System.out.println("DAOFactory: Modalità DEMO (RAM) impostata su " + active);
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
            // In demo mode usiamo la classe finta per popolare la tabella
            return new InMemoryPersonalTrainerDAO();
        } else {
            return null;
        }
    }

    public static WorkoutPlanDAO getWorkoutPlanDAO() {
        // 1. Priorità alla DEMO MODE (Requisito: In-Memory Only)
        if (isDemoMode) {
            return new InMemoryWorkoutPlanDAO();
        }

        // 2. Se siamo in FULL MODE, scegliamo la persistenza
        if (CONF_USE_FILESYSTEM) {
            System.out.println("DAOFactory: Utilizzo FileSystemDAO");
            return new FileSystemWorkoutPlanDAO(); // <--- Il nuovo DAO
        } else {
            System.out.println("DAOFactory: Utilizzo MySQLDAO");
            return new MySQLWorkoutPlanDAO();
        }
    }

    public static ExerciseDAO getExerciseDAO() {
        return new RestApiExerciseDAO();
    }
}