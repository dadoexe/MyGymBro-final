package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Exercise;

import java.util.List;

public interface ExerciseDAO {

    Exercise findByName(String name) throws DAOException;

    List<Exercise> findAll() throws DAOException;

    List<Exercise> search(String keyword) throws DAOException;
}