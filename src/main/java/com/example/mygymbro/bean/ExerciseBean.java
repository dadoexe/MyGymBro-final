package com.example.mygymbro.bean;

public class ExerciseBean {

    private String id;
    private String name;
    private String description;
    private String muscleGroup;
    private String gifUrl;

    public ExerciseBean() {
        // Costruttore vuoto obbligatorio
    }

    // Costruttore di utilità
    public ExerciseBean(String id, String name, String description, String muscleGroup) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
    }

    // --- Getter e Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    @Override
    public String toString() {
        // Utile per popolare le ComboBox di JavaFX automaticamente
        return name;
    }
}