package dao;

import java.sql.*;
import javax.swing.JOptionPane;

public class ConnectionProvider{
    private static Connection con;
    public static Connection getCon(){
        try{
            //if(con == null){
                //driver class load
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                //create a connection
                con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/busbooking", 
                        "root", 
                        ""
                );
                System.out.println("Database Connected!");
            //}
        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Connection Failed! Check:\n"
                + "1. MySQL Server is running\n"
                + "2. Database name is correct\n"
                + "3. Credentials are correct",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        return con;
    }
}

