package com.example.mygymbro.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DAOUtils {

    public static void closeConnection(Connection conn) {
        try{
            if(conn != null){
                conn.close();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void closeStatement(Statement stmt){
       try{if(stmt != null){
            stmt.close();
        }
    }catch(SQLException e){
       e.printStackTrace();}
    }

    public static void closeResultSet(ResultSet rs){
      try{if(rs != null){
            rs.close();
        }
    }catch(SQLException e){
          e.printStackTrace();
      }
    }
    // Molto comodo per pulire tutto con una riga sola nel finally!
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
}

