package com.example.mygymbro.bean;

public class WorkoutExerciseBean {

    private String exerciseName; // Solo il nome, non tutto l'oggetto Exercise
    private int sets;
    private int reps;
    private int restTime;
    private String muscleGroup;

    // Costruttore vuoto richiesto per la serializzazione e il framework JavaBeans
    public WorkoutExerciseBean() {
        // Costruttore di default vuoto necessario per JavaBeans specification
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

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

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }
}