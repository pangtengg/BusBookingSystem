package gui;

import dao.ConnectionProvider;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class AdminLogin{
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public AdminLogin(){
        frame = new JFrame("Admin Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        
        adminLogin();
        
        frame.setVisible(true);
    }
    
    private void adminLogin(){
        JLabel labelAdmin = new JLabel("Admin Username");
        labelAdmin.setBounds(30, 50, 150, 25);
        frame.add(labelAdmin);
        
        usernameField = new JTextField();
        usernameField.setBounds(180, 50, 180, 25);
        frame.add(usernameField);
        
        JLabel labelPassword = new JLabel("Password");
        labelPassword.setBounds(30, 100, 150, 25);
        frame.add(labelPassword);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(180, 100, 180, 25);
        frame.add(passwordField);
        
        JButton submitButton = new JButton("Login");
        submitButton.setBounds(150, 160, 100, 30);
        frame.add(submitButton);
        
        submitButton.addActionListener(e-> loginAdmin());
}
    private void loginAdmin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConnectionProvider.getCon()) {
            // Query to check admin credentials
            String query = "SELECT userID, username, password FROM users WHERE username = ? AND userType = 'ADMIN'";
            try (PreparedStatement stmt = con.prepareStatement(query)){
                stmt.setString(1, username);
                
                try (ResultSet rs = stmt.executeQuery()){
                    if (rs.next()) {
                        int adminID = rs.getInt("userID");
                        String adminName = rs.getString("username");
                        String hashedPassword = rs.getString("password");
                        
                        if(BCrypt.checkpw(password, hashedPassword)){
                        JOptionPane.showMessageDialog(frame, "Login Successful! Welcome, " + adminName, "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        frame.dispose();

                        // Launch dashboard on EDT
                        SwingUtilities.invokeLater(() -> new AdminDashboard(adminID, adminName).setVisible(true));                
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    
