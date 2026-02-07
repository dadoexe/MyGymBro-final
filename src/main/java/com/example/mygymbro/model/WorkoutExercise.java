package com.example.mygymbro.model;

import com.example.mygymbro.bean.WorkoutPlanBean;

import java.util.List;

/**
 * Questa classe rappresenta un esercizio all'interno di una scheda specifica.
 * Contiene i parametri di esecuzione (serie, ripetizioni, recupero)
 * e il riferimento all'esercizio generico (es. Panca Piana).
 */
public class WorkoutExercise {

    private int sets;
    private int reps;
    private int restTime; // Inteso in secondi (come da best practice)
    private Exercise exercise;
    // Associazione con l'oggetto Exercise (il "catalogo")
    private Exercise exerciseDefinition;


    // Costruttore Vuoto
    public WorkoutExercise() {
    }

    // Costruttore Completo
    public WorkoutExercise(Exercise exerciseDefinition, int sets, int reps, int restTime) {
        this.exerciseDefinition = exerciseDefinition;
        this.exercise = exerciseDefinition;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
    }

    // --- METODI DI BUSINESS LOGIC (Non CRUD) ---

    /**
     * Calcola il volume totale di ripetizioni per questo esercizio.
     * Come da UML, ritorna un float.
     */
    public float calculateVolume() {
        // Se avessi il campo "peso", faresti: sets * reps * weight
        // Basandoci sul diagramma attuale (senza peso), calcoliamo il volume di ripetizioni totali.
        return (float) (sets * reps);
    }

    /**
     * Calcola la durata stimata in minuti per completare questo esercizio.
     * Assume una durata media di esecuzione di 45 secondi per serie.
     */
    public int getEstimatedDurationMinutes() {
        int executionTimePerSet = 45; // secondi stimati sotto tensione
        int totalSeconds = sets * (executionTimePerSet + restTime);
        return totalSeconds / 60; // Ritorna i minuti
    }

    // --- GETTER E SETTER STANDARD ---

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public Exercise getExerciseDefinition() {
        return exerciseDefinition;
    }

    public void setExerciseDefinition(Exercise exerciseDefinition) {
        this.exerciseDefinition = exerciseDefinition;
    }
    public Exercise getExercise() {
        return exercise;
    }

    @Override
    public String toString() {
        // Utile per il debug o per le ListView semplici
        if (exerciseDefinition != null) {
            return exerciseDefinition.getName() + ": " + sets + "x" + reps + " (rec. " + restTime + "s)";
        }
        return "Esercizio non definito";
    }


}