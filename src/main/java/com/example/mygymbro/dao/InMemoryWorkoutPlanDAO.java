package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryWorkoutPlanDAO implements WorkoutPlanDAO {

    private static List<WorkoutPlan> ramPlans = new ArrayList<>();

    @Override
    public void save(WorkoutPlan plan) throws DAOException {
        // Se è una nuova scheda (ID 0 o non esiste), assegniamo un nuovo ID
        if (plan.getId() == 0) {
            int newId = ramPlans.isEmpty() ? 1 : ramPlans.get(ramPlans.size() - 1).getId() + 1;
            plan.setId(newId);
        } else {
            // Se esiste già (modifica), rimuoviamo la vecchia versione
            delete(plan.getId());
        }

        // Salviamo la nuova versione
        ramPlans.add(plan);
        System.out.println("[RAM DB] Piano salvato: " + plan.getName() + " (ID: " + plan.getId() + ")");
    }

    @Override
    public List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException {
        if (athlete == null) return new ArrayList<>();

        return ramPlans.stream()
                // FILTRO SICURO: Controlliamo che la scheda abbia un atleta e che l'ID corrisponda
                .filter(p -> p.getAthlete() != null && p.getAthlete().getId() == athlete.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(int id) throws DAOException {
        // Rimuove la scheda se l'ID corrisponde
        boolean removed = ramPlans.removeIf(p -> p.getId() == id);
        if (removed) {
            System.out.println("[RAM DB] Piano eliminato: ID " + id);
        }
    }

    @Override
    public void update(WorkoutPlan plan) throws DAOException {
        save(plan); // In memoria, update e save sono la stessa cosa (sovrascrittura)
    }
}