package com.example.mygymbro.dao;

import com.example.mygymbro.model.Exercise;

import java.sql.SQLException;
import java.util.List;

public interface ExerciseDAO {

    Exercise findByName(String name) throws SQLException;
    List<Exercise> findAll()  throws SQLException;
    List<Exercise> search(String keyword);
}
