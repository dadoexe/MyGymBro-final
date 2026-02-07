package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemoryWorkoutPlanDAO implements WorkoutPlanDAO {

    // 1. LOGGER: Sostituisce System.out (Security/Reliability)
    private static final Logger logger = Logger.getLogger(InMemoryWorkoutPlanDAO.class.getName());

    private static final List<WorkoutPlan> ramPlans = new ArrayList<>();

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

        ramPlans.add(plan);
        // CORREZIONE: Uso del logger invece di System.out
        logger.log(Level.INFO, "[RAM DB] Piano salvato: {0} (ID: {1})", new Object[]{plan.getName(), plan.getId()});
    }

    @Override
    public List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException {
        if (athlete == null) return new ArrayList<>();

        // CORREZIONE: .toList() è il modo moderno (Java 16+) per chiudere gli stream
        // Se usi Java 8/11, usa .collect(java.util.stream.Collectors.toList())
        return ramPlans.stream()
                .filter(p -> p.getAthlete() != null && p.getAthlete().getId() == athlete.getId())
                .toList();
    }

    @Override
    public void delete(int id) throws DAOException {
        boolean removed = ramPlans.removeIf(p -> p.getId() == id);
        if (removed) {
            logger.log(Level.INFO, "[RAM DB] Piano eliminato: ID {0}", id);
        }
    }

    @Override
    public void update(WorkoutPlan plan) throws DAOException {
        save(plan); // In memoria, update e save sono la stessa cosa (sovrascrittura)
    }
}