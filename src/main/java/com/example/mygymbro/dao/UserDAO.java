package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.User;

public interface UserDAO {

    User findByUsername(String username) throws DAOException;

    void save(User user) throws DAOException;
}