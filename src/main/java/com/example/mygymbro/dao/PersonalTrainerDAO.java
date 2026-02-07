package com.example.mygymbro.dao;

import com.example.mygymbro.model.Athlete;
import java.util.List;

public interface PersonalTrainerDAO {
    List<Athlete> findAllAthletes();
}