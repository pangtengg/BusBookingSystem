/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
/**
 *
 * @author pangt
 */
public class UserDashboard extends JFrame {
    private final int userID;
    private JDesktopPane desktopPane;
    private UserBookingInternalFrame bookingFrame;
    
    public UserDashboard(int userID) {
        this.userID = userID;

        setTitle("User Dashboard - ID: " + userID);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);

        bookingFrame = new UserBookingInternalFrame(userID);
        desktopPane.add(bookingFrame);
        
        // Center the internal frame
        centerInternalFrame(bookingFrame);
        
        // Add resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerInternalFrame(bookingFrame);
            }
        });
        
        setVisible(true);
    } 
    private void centerInternalFrame(JInternalFrame frame) {
        Dimension desktopSize = desktopPane.getSize();
        Dimension frameSize = frame.getSize();
        frame.setLocation(
            (desktopSize.width - frameSize.width) / 2,
            (desktopSize.height - frameSize.height) / 2
        );
    }
}
       
    
    

