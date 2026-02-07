package com.example.mygymbro.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAOUtils {

    // 1. Logger per evitare e.printStackTrace()
    private static final Logger logger = Logger.getLogger(DAOUtils.class.getName());

    // 2. Costruttore privato per nascondere quello pubblico implicito
    // (Regola Sonar: "Utility classes should not have public constructors")
    private DAOUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Errore durante la chiusura della Connection", e);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Errore durante la chiusura dello Statement", e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Errore durante la chiusura del ResultSet", e);
            }
        }
    }

    // Metodo helper "tutto in uno"
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    // Overload utile se non hai il ResultSet
    public static void close(Connection conn, Statement stmt) {
        closeStatement(stmt);
        closeConnection(conn);
    }
}

