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
            // Se l'oggetto è un'istanza di Athlete, lo aggiunge alla lista
            if (u instanceof Athlete) {
                athletes.add((Athlete) u);
            }
        }
        return athletes;
    }
}