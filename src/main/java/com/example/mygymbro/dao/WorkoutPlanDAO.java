package com.example.mygymbro.dao;

import com.example.mygymbro.bean.WorkoutPlanBean; // Se serve
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;
import com.example.mygymbro.exceptions.DAOException; // Importa la tua eccezione

import java.util.List;

public interface WorkoutPlanDAO {
    void save(WorkoutPlan plan) throws DAOException;
    void update(WorkoutPlan plan) throws DAOException;
    void delete(int planId) throws DAOException;
    List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException;
}