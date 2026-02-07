package com.example.mygymbro.dao;

import com.example.mygymbro.model.Exercise;
import com.example.mygymbro.model.MuscleGroup;
import com.example.mygymbro.utils.DAOUtils;
import com.example.mygymbro.utils.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLExerciseDAO implements ExerciseDAO {

    private Exercise mapRowToExercise(ResultSet rs) throws SQLException {
        // Qui metti la logica di creazione UNA VOLTA sola
        return new Exercise(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                MuscleGroup.valueOf(rs.getString("muscle_group").toUpperCase())
        );
    }

    @Override
    public Exercise findByName(String name) throws SQLException {
        //definisco la query
        String query = "SELECT * FROM exercise WHERE name=?";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Exercise exercise = null;
        try{
            conn = DBConnect.getConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, name);
            rs = statement.executeQuery();
            if(rs.next()){

                exercise = mapRowToExercise(rs);
            }

        }finally{
            DAOUtils.close(conn, statement, rs);
        }
            return exercise;
    }

    @Override
    public List<Exercise> findAll()  throws SQLException {

        String query = "SELECT * FROM exercise ORDER BY name ASC";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        //inizializzo lista vuota per riempirla in seguito
        List<Exercise> exercises = new ArrayList<Exercise>();
        try{
            conn = DBConnect.getConnection();
            statement = conn.prepareStatement(query);
            rs = statement.executeQuery();
            //finche ci sono righe
            while (rs.next()) {
                exercises.add(mapRowToExercise(rs)); // Riutilizzo!
            }

        }finally{
            DAOUtils.close(conn, statement, rs);
        }
        return exercises;
    }

    @Override
    public List<Exercise> search(String keyword) {
        return List.of();
    }
}
