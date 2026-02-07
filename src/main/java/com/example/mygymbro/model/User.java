package com.example.mygymbro.model;

public class User { // <--- ABSTRACT!
    private int id;
    private String username;
    private String password;
    private String name;
    private String cognome;
    private String email; // Nel diagramma c'è email, non cognome

    // Costruttore per le sottoclassi
    public User() {
    }

    public User(int id, String username, String password, String name, String cognome, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.cognome= cognome;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
    public void setNome(String name) {
        this.name = name;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}