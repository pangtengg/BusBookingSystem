package dao;
import javax.swing.JOptionPane;

public class tables {
    public static void main(String[] args) {
        try {
            // Create database
            String createDatabase = "CREATE DATABASE IF NOT EXISTS busbooking";
            DbOperations.setDataOrDelete(createDatabase, null);

            // Use the database
            String useDatabase = "USE busbooking";
            DbOperations.setDataOrDelete(useDatabase, null);
            
            // Create users table
            String userTable = "CREATE TABLE IF NOT EXISTS users ("
                + "userID INT PRIMARY KEY AUTO_INCREMENT, "
                + "username VARCHAR(100) NOT NULL, "
                + "password VARCHAR(255) NOT NULL, "
                + "email VARCHAR(100) UNIQUE NOT NULL, "
                + "userType ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER')";
            DbOperations.setDataOrDelete(userTable, null);

            // Create buses table
            String busTable = "CREATE TABLE IF NOT EXISTS buses ("
                + "busID VARCHAR(10) PRIMARY KEY, "
                + "source VARCHAR(100) NOT NULL, "
                + "destination VARCHAR(100) NOT NULL, "
                + "departureDate DATE NOT NULL, "
                + "departureTime TIMESTAMP NOT NULL, "
                + "totalSeats INT NOT NULL, "
                + "ticketPrice DECIMAL(10,2) NOT NULL)";
            DbOperations.setDataOrDelete(busTable, null);

            // Alter buses table to add departureDate if it does not exist
            /* try (Connection con = ConnectionProvider.getCon();
                 Statement stmt = con.createStatement()) {
                String alterTable = "ALTER TABLE buses ADD COLUMN IF NOT EXISTS departureDate DATE";
                stmt.execute(alterTable);
            }*/
            
            // Create booking table 
            String bookingTable = "CREATE TABLE IF NOT EXISTS booking ("
                + "bookingID VARCHAR(50) PRIMARY KEY, "
                + "busID VARCHAR(50) NOT NULL, "
                + "userID INT NOT NULL, "
                + "seatNumber INT NOT NULL, "
                + "customerName VARCHAR(100) NOT NULL, "
                + "ticketPrice DECIMAL(10,2) NOT NULL, "
                + "bookingDateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "STATUS ENUM('CONFIRMED', 'CANCELLED', 'PENDING') DEFAULT 'CONFIRMED', "
                + "FOREIGN KEY (userID) REFERENCES users(userID), "
                + "FOREIGN KEY (busID) REFERENCES buses(busID), "
                + "UNIQUE KEY unique_seat_bus (busID, seatNumber))";
            DbOperations.setDataOrDelete(bookingTable, null);
            
            JOptionPane.showMessageDialog(null, "Database initialized successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
