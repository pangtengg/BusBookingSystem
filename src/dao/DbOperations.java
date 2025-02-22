package dao;

import javax.swing.JOptionPane;
import java.sql.*;

public class DbOperations {
    public static void setDataOrDelete(String Query, String msg){
        try{
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            st.executeUpdate(Query);
            if(msg != null && !msg.isEmpty()){
                JOptionPane.showMessageDialog(null, msg);
        } 
        } catch(Exception e){
            JOptionPane.showInternalMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }            
    }
}

