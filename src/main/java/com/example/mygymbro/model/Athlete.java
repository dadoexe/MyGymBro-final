package com.example.mygymbro.model;
import java.util.ArrayList;
import java.util.List;

public class Athlete extends User {

    private float weight;
    private float height;
    private int age;

    private List<WorkoutPlan> pianiUtente = new ArrayList<>();

    public Athlete(){
        super();
    }
    public Athlete(int id, String username, String password, String name, int age, String email, String cognome, float weight, float height) {
        super(id, username, password, name,cognome, email); // Passa i dati comuni al padre
        this.weight = weight;
        this.height = height;
        this.age = age;

    }

    public float getWeight() {
        return weight;
    }

    public float getHeight() {
        return height;
    }

    public int getAge() {
        return age;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void addWorkoutPlan(WorkoutPlan workoutPlan){
        this.pianiUtente.add(workoutPlan);
    }



}
