package gui;

import dao.tables;
import javax.swing.*;
import java.awt.*;


public class Main {
    private JFrame frame;
    private JPanel panel;
    
    public Main() {
        tables.main(new String[]{});
        
        frame = new JFrame("User Selection");
        panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton adminButton = new JButton("Admin");
        JButton userButton = new JButton("User");

        adminButton.addActionListener(e -> showOptions("Admin"));
        userButton.addActionListener(e -> showOptions("User"));

        panel.add(adminButton);
        panel.add(userButton);

        frame.add(panel);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showOptions(String role) {
        panel.removeAll();
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel label = new JLabel(role + " Options", SwingConstants.CENTER);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back");
        
        loginButton.addActionListener(e -> login(role));
        registerButton.addActionListener(e -> register(role));
        backButton.addActionListener(e -> resetView());
        
        panel.add(label);
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(backButton);

        panel.revalidate();
        panel.repaint();
    }
    
    private void login(String role) {
        frame.dispose(); // Close selection window

        if (role.equals("Admin")) {
            new AdminLogin(); // Should lead to AdminDashboard if successful
        } else {
            new UserLogin(); // Should lead to UserDashboard if successful
        }
    }

    private void register(String role) {
        frame.dispose(); // Close selection window

        if (role.equals("Admin")) {
            new AdminRegister(); 
        } else {
            new UserRegister();
        }
    }

      private void resetView() {
        panel.removeAll();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton adminButton = new JButton("Admin");
        JButton userButton = new JButton("User");

        adminButton.addActionListener(e -> showOptions("Admin"));
        userButton.addActionListener(e -> showOptions("User"));

        panel.add(adminButton);
        panel.add(userButton);

        panel.revalidate();
        panel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}


