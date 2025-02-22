package gui;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BusFileManager {
    private static final String LOG_FILE = "bus_system.log";
    
    // Export bus data to CSV file
    public static void exportBusDataToFile(JFrame parentFrame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Bus Data");
        fileChooser.setSelectedFile(new File("bus_data.csv"));
        
        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            try (Connection conn = ConnectionProvider.getCon();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM buses");
                 PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                
                // Write CSV header
                writer.println("BusID,Source,Destination,DepartureDate,DepartureTime,TotalSeats,TicketPrice");
                
                // Write data rows
                while (rs.next()) {
                    writer.printf("%s,%s,%s,%s,%s,%d,%.2f\n",
                        rs.getString("busID"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getDate("departureDate"),
                        rs.getTime("departureTime"),
                        rs.getInt("totalSeats"),
                        rs.getDouble("ticketPrice")
                    );
                }
                
                JOptionPane.showMessageDialog(parentFrame, "Data exported successfully!");
                logAction("Bus data exported to " + fileChooser.getSelectedFile().getName());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame, "Error exporting data: " + e.getMessage());
                logAction("Error exporting bus data: " + e.getMessage());
            }
        }
    }
    
    // Import bus data from CSV file
public static void importBusDataFromFile(JFrame parentFrame) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Bus Data");
    
    if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
             Connection conn = ConnectionProvider.getCon()) {
            
            String line = reader.readLine(); // Skip header
            if (line == null) {
                throw new IOException("File is empty");
            }
            
            // Validate header format
            String[] headers = line.split(",");
            if (headers.length != 7) {
                throw new IOException("Invalid CSV format: Expected 7 columns in header");
            }
            
            String sql = "INSERT INTO buses (busID, source, destination, departureDate, departureTime, totalSeats, ticketPrice) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int lineNumber = 1; // Start counting after header
            
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String[] data = line.split(",");
                    
                    // Validate row data
                    if (data.length != 7) {
                        throw new IOException("Invalid data format at line " + lineNumber + ": Expected 7 columns");
                    }
                    
                    try {
                        // Validate required fields are not empty
                        for (int i = 0; i < data.length; i++) {
                            if (data[i] == null || data[i].trim().isEmpty()) {
                                throw new IOException("Empty value found at line " + lineNumber + ", column " + (i + 1));
                            }
                        }
                        
                        pst.setString(1, data[0].trim()); // busID
                        pst.setString(2, data[1].trim()); // source
                        pst.setString(3, data[2].trim()); // destination
                        
                        // Parse and convert date from M/D/YYYY to YYYY-MM-DD

                    try {
                        // Parse the date from M/D/YYYY to YYYY-MM-DD
                        java.text.SimpleDateFormat dateInputFormat = new java.text.SimpleDateFormat("M/d/yyyy");
                        java.text.SimpleDateFormat dateOutputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date parsedDate = dateInputFormat.parse(data[3].trim());
                        String formattedDate = dateOutputFormat.format(parsedDate); // e.g., "2025-02-14"

                        pst.setDate(4, java.sql.Date.valueOf(formattedDate));
    
                        // Handle different time formats (Excel may export 12-hour or 24-hour format)
                        String timeStr = data[4].trim();
                        java.util.Date parsedTime;
    
                        if (timeStr.matches(".*(AM|PM).*")) {
                            // If AM/PM exists, use 12-hour format
                            java.text.SimpleDateFormat timeInputFormat = new java.text.SimpleDateFormat("hh:mm:ss a");
                            parsedTime = timeInputFormat.parse(timeStr);
                        } else {
                            // Otherwise, assume 24-hour format
                            java.text.SimpleDateFormat timeInputFormat = new java.text.SimpleDateFormat("H:mm:ss");
                            parsedTime = timeInputFormat.parse(timeStr);
                        }
    
                        // Convert time to 24-hour format
                        java.text.SimpleDateFormat timeOutputFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                        String formattedTime = timeOutputFormat.format(parsedTime); // e.g., "09:00:00"

                        // 🔹 Combine date and time into full TIMESTAMP format (YYYY-MM-DD HH:MM:SS)
                        String dateTimeStr = formattedDate + " " + formattedTime; // e.g., "2025-02-14 09:00:00"

                        System.out.println("Inserting TIMESTAMP: " + dateTimeStr); // Debugging Output

                        // Insert as TIMESTAMP
                        pst.setTimestamp(5, java.sql.Timestamp.valueOf(dateTimeStr));

                    } catch (Exception e) {
                        throw new IOException("Invalid date/time format at line " + lineNumber + ": " + data[3] + " " + data[4]);
                    }
                       
                        // Validate and parse totalSeats
                        try {
                            int seats = Integer.parseInt(data[5].trim());
                            if (seats <= 0) {
                                throw new IOException("Invalid seat count at line " + lineNumber + ": Must be positive");
                            }
                            pst.setInt(6, seats);
                        } catch (NumberFormatException e) {
                            throw new IOException("Invalid seat count at line " + lineNumber + ": " + data[5]);
                        }
                        
                        // Validate and parse ticketPrice
                        try {
                            double price = Double.parseDouble(data[6].trim());
                            if (price <= 0) {
                                throw new IOException("Invalid ticket price at line " + lineNumber + ": Must be positive");
                            }
                            pst.setDouble(7, price);
                        } catch (NumberFormatException e) {
                            throw new IOException("Invalid ticket price at line " + lineNumber + ": " + data[6]);
                        }
                        
                        pst.executeUpdate();
                        
                    } catch (SQLException e) {
                        throw new IOException("Database error at line " + lineNumber + ": " + e.getMessage());
                    }
                }
                
                JOptionPane.showMessageDialog(parentFrame, "Data imported successfully!");
                logAction("Bus data imported from " + fileChooser.getSelectedFile().getName());
                
            }
        } catch (Exception e) {
            String errorMessage = "Error importing data: " + e.getMessage();
            JOptionPane.showMessageDialog(parentFrame, errorMessage);
            logAction(errorMessage);
        }
    }
}
    
    // View system logs
    public static void viewLogs(JFrame parentFrame) {
        try {
            List<String> logs = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logs.add(line);
                }
            }
            
            JTextArea textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            for (String log : logs) {
                textArea.append(log + "\n");
            }
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            JDialog dialog = new JDialog(parentFrame, "System Logs", true);
            dialog.add(scrollPane);
            dialog.pack();
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame, "Error reading logs: " + e.getMessage());
        }
    }
    
    // Log system actions
    public static void logAction(String action) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println(timestamp + " - " + action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}