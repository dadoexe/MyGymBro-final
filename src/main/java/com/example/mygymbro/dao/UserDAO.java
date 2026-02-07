package com.example.mygymbro.dao;

import com.example.mygymbro.model.User;

import java.sql.SQLException;

public interface UserDAO {

    User findByUsername(String username) throws SQLException; // login
    void save(User user) throws SQLException;
}
