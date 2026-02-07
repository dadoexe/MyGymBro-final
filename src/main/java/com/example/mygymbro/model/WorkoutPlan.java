package com.example.mygymbro.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class WorkoutPlan {

    private int id;
    private String name;
    private Date creationDate;
    private String comment;
    private List<WorkoutExercise> exercises = new ArrayList<>();
    private Athlete athlete;

    // Costruttore Completo
    public WorkoutPlan(int id, String name, String comment, Date creationDate, Athlete athlete) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.creationDate = creationDate;
        this.athlete = athlete; // Assegnazione
    }

    public void addExercise(WorkoutExercise ex) {

        exercises.add(ex);
    }
    public int getAthleteId() {
        return athlete.getId();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void removeExercise(WorkoutExercise ex) {
        exercises.remove(ex);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public List<WorkoutExercise> getExercises() {
        return exercises;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }
    public Athlete getAthlete() {
        return athlete;
    }

    public int getEstimatedMinutes() {
        int totalSeconds = 0;
        for(WorkoutExercise ex : exercises) {
            totalSeconds += ex.getSets() * (45 + ex.getRestTime()); // 45s media per serie
        }
        return totalSeconds / 60;
    }

}
