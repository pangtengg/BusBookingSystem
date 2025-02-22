package gui;

import dao.ConnectionProvider;
import gui.UserDashboard;
import javax.swing.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UserLogin{
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public UserLogin(){
        initialize();
    }     
        
    private void initialize(){    
        frame = new JFrame("User Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        
        setupComponents();
        
        frame.setVisible(true);
    }
    
    private void setupComponents(){
        JLabel labelUser = new JLabel("Username: ");
        labelUser.setBounds(30, 50, 150, 25);
        frame.add(labelUser);
        
        usernameField = new JTextField();
        usernameField.setBounds(180, 50, 180, 25);
        frame.add(usernameField);
        
        JLabel labelPassword = new JLabel("Password: ");
        labelPassword.setBounds(30, 100, 150, 25);
        frame.add(labelPassword);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(180, 100, 180, 25);
        frame.add(passwordField);
        
        JButton submitButton = new JButton("Login");
        submitButton.setBounds(150, 160, 100, 30);
        submitButton.addActionListener(e -> loginUser());
        frame.add(submitButton);
    }
    
    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConnectionProvider.getCon()) {
            String query = "SELECT userID, password, username FROM users WHERE username = ? AND userType = 'USER'";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, username);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userID = rs.getInt("userID");
                        String dbUsername = rs.getString("username");
                        String hashedPassword = rs.getString("password");
                        
                        if(BCrypt.checkpw(password, hashedPassword)){
                        JOptionPane.showMessageDialog(frame, 
                            "Login Successful! Welcome, " + dbUsername, 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        frame.dispose();
                        
                        // Launch dashboard on EDT
                        SwingUtilities.invokeLater(() -> new UserDashboard(userID));
                    } else {
                        JOptionPane.showMessageDialog(frame,
                            "Invalid username or password!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }else{
                        JOptionPane.showMessageDialog(frame,
                                "Invalid username or password!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
            }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame,
                "Database Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}



 