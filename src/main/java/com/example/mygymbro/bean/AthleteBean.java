package com.example.mygymbro.bean;

public class AthleteBean extends UserBean {
    private float weight; // O String se vuoi validare dopo
    private float height;
     // Aggiunto per chiarezza


    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
}
