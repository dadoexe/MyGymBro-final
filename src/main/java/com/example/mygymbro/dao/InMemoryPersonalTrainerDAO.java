package com.example.mygymbro.dao;

import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.User;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPersonalTrainerDAO implements PersonalTrainerDAO {

    @Override
    public List<Athlete> findAllAthletes() {
        List<Athlete> athletes = new ArrayList<>();

        // Scorre la lista statica condivisa in InMemoryUserDAO
        for (User u : InMemoryUserDAO.ramDB) {
            // CORREZIONE: Pattern Matching (Java 14+)
            // Invece di fare il check e poi il cast ((Athlete) u), facciamo tutto insieme.
            if (u instanceof Athlete athlete) {
                athletes.add(athlete);
            }
        }
        return athletes;
    }
}