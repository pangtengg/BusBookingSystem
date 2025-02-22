package gui;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class AdminBus extends JInternalFrame {
    private JDesktopPane desktop;

    public AdminBus() {
        super("Bus Admin Management", true, true, true, true);
        initComponents();
        setSize(600, 400);
        setVisible(true);
    }

    private void initComponents() {
        desktop = new JDesktopPane();
        setContentPane(desktop);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Bus");
        JButton modifyButton = new JButton("Modify Bus");
        JButton deleteButton = new JButton("Delete Bus");
        JButton viewButton = new JButton("View Bus");

        buttonPanel.add(addButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);

        addButton.addActionListener(e -> showAddBusFrame());
        modifyButton.addActionListener(e -> showModifyBusFrame());
        deleteButton.addActionListener(e -> showDeleteBusFrame());
        viewButton.addActionListener(e -> showViewBusesFrame());

        add(buttonPanel, BorderLayout.NORTH);
    }

    private void showAddBusFrame() {
        AddBusInternalFrame addFrame = new AddBusInternalFrame();
        desktop.add(addFrame);
        try {
            addFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}
    }

    private void showModifyBusFrame() {
        ModifyBusInternalFrame modifyFrame = new ModifyBusInternalFrame();
        desktop.add(modifyFrame);
        try {
            modifyFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}
    }

    private void showDeleteBusFrame() {
        DeleteBusInternalFrame deleteFrame = new DeleteBusInternalFrame();
        desktop.add(deleteFrame);
        try {
            deleteFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}
    }

    private void showViewBusesFrame() {
        ViewBusesInternalFrame viewFrame = new ViewBusesInternalFrame();
        desktop.add(viewFrame);
        try { 
            viewFrame.setSelected(true); 
        } catch (java.beans.PropertyVetoException e) {}
    }
}

class ViewBusesInternalFrame extends JInternalFrame {
    private JTable table;

    public ViewBusesInternalFrame() {
        super("View All Buses", true, true, true, true);
        initComponents();
        setSize(800, 600);
        setVisible(true);
    }

    private void initComponents() {
        JScrollPane scrollPane = new JScrollPane();
        table = new JTable();
        setupTable();
        loadBusData();
        scrollPane.setViewportView(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void setupTable() {
        String[] columns = {"Bus ID", "Source", "Destination", "Departure Date", "Departure Time", 
                            "Total Seats", "Price"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
    }

    private void loadBusData() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        try (Connection conn = ConnectionProvider.getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM buses")) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("busID"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getDate("departureDate"),
                    rs.getTime("departureTime"),
                    rs.getInt("totalSeats"),
                    rs.getDouble("ticketPrice")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading buses: " + ex.getMessage());
        }
    }
}

class AddBusInternalFrame extends JInternalFrame {
    private JTextField busIdField, sourceField, destField, seatsField, priceField, departureTimeField;
    private JDateChooser dateChooser;

    public AddBusInternalFrame() {
        super("Add New Bus", true, true, true, true);
        initComponents();
        setSize(400, 350);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new GridLayout(8, 2, 5, 5));

        add(new JLabel("Bus ID:"));
        busIdField = new JTextField();
        add(busIdField);

        add(new JLabel("Source:"));
        sourceField = new JTextField();
        add(sourceField);

        add(new JLabel("Destination:"));
        destField = new JTextField();
        add(destField);

        add(new JLabel("Departure Date:"));
        dateChooser = new JDateChooser();
        add(dateChooser);

        add(new JLabel("Departure Time (HH:mm):"));
        departureTimeField = new JTextField();
        add(departureTimeField);

        add(new JLabel("Total Seats:"));
        seatsField = new JTextField();
        add(seatsField);

        add(new JLabel("Ticket Price:"));
        priceField = new JTextField();
        add(priceField);

        JButton submitButton = new JButton("Add Bus");
        submitButton.addActionListener(e -> addBusToDatabase());
        add(submitButton);
        BusFileManager.logAction("Added new bus: " + busIdField.getText());
    }

    private void addBusToDatabase() {
        try {
            if (busIdField.getText().trim().isEmpty() || 
                sourceField.getText().trim().isEmpty() || 
                destField.getText().trim().isEmpty() || 
                dateChooser.getDate() == null || 
                departureTimeField.getText().trim().isEmpty() || 
                seatsField.getText().trim().isEmpty() || 
                priceField.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "All fields must be filled out!");
                return;
            }

            Connection conn = ConnectionProvider.getCon();
            String sql = "INSERT INTO buses (busID, source, destination, departureDate, departureTime, totalSeats, ticketPrice) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, busIdField.getText().trim());
                pst.setString(2, sourceField.getText().trim());
                pst.setString(3, destField.getText().trim());

                // Format and set the departure date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String departureDate = dateFormat.format(dateChooser.getDate());
                pst.setDate(4, java.sql.Date.valueOf(departureDate));

                // Format and set the departure time
                String departureDateTime = departureDate + " " + departureTimeField.getText().trim() + ":00";
                pst.setTimestamp(5, Timestamp.valueOf(departureDateTime));

                pst.setInt(6, Integer.parseInt(seatsField.getText().trim()));
                pst.setDouble(7, Double.parseDouble(priceField.getText().trim()));

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Bus added successfully!");
                dispose();
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error adding bus: " + ex.getMessage());
        }
    }
}

class ModifyBusInternalFrame extends JInternalFrame {
    private JComboBox<String> busIdCombo;
    private JTextField sourceField, destField, departureTimeField, seatsField, priceField;
    private JDateChooser dateChooser;

    public ModifyBusInternalFrame() {
        super("Modify Bus", true, true, true, true);
        initComponents();
        setSize(400, 350);  // Adjust size to accommodate new field
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new GridLayout(8, 2, 5, 5));  // Adjust grid layout

        add(new JLabel("Select Bus ID:"));
        busIdCombo = new JComboBox<>();
        loadBusIds();
        add(busIdCombo);
        busIdCombo.addActionListener(e -> loadBusDetails());

        add(new JLabel("Source:"));
        sourceField = new JTextField();
        add(sourceField);

        add(new JLabel("Destination:"));
        destField = new JTextField();
        add(destField);

        add(new JLabel("Departure Date:"));
        dateChooser = new JDateChooser();
        add(dateChooser);

        add(new JLabel("Departure Time (HH:mm):"));
        departureTimeField = new JTextField();
        add(departureTimeField);

        add(new JLabel("Total Seats:"));
        seatsField = new JTextField();
        add(seatsField);

        add(new JLabel("Ticket Price:"));
        priceField = new JTextField();
        add(priceField);

        JButton updateButton = new JButton("Update Bus");
        updateButton.addActionListener(e -> updateBus());
        add(updateButton);
        BusFileManager.logAction("Modified bus: " + busIdCombo.getSelectedItem());
    }

    private void loadBusIds() {
        try {
            Connection conn = ConnectionProvider.getCon();
            String sql = "SELECT busID FROM buses";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    busIdCombo.addItem(rs.getString("busID"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bus IDs: " + ex.getMessage());
        }
    }

    private void loadBusDetails() {
        String selectedBusId = (String) busIdCombo.getSelectedItem();
        if (selectedBusId == null) return;

        try {
            Connection conn = ConnectionProvider.getCon();
            String sql = "SELECT * FROM buses WHERE busID = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, selectedBusId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        sourceField.setText(rs.getString("source"));
                        destField.setText(rs.getString("destination"));
                        
                        // Separate the date and time
                        LocalDateTime departureDateTime = rs.getTimestamp("departureTime").toLocalDateTime();
                        dateChooser.setDate(java.sql.Date.valueOf(departureDateTime.toLocalDate()));
                        departureTimeField.setText(departureDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

                        seatsField.setText(String.valueOf(rs.getInt("totalSeats")));
                        priceField.setText(String.valueOf(rs.getDouble("ticketPrice")));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bus details: " + ex.getMessage());
        }
    }

    private void updateBus() {
        try {
            Connection conn = ConnectionProvider.getCon();
            String sql = "UPDATE buses SET source=?, destination=?, departureDate=?, departureTime=?, totalSeats=?, ticketPrice=? WHERE busID=?";  // Update query

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, sourceField.getText());
                pst.setString(2, destField.getText());

                // Format and set the departure date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String departureDate = dateFormat.format(dateChooser.getDate());
                pst.setDate(3, java.sql.Date.valueOf(departureDate));

                // Format and set the departure time
                String departureDateTime = departureDate + " " + departureTimeField.getText() + ":00";
                pst.setTimestamp(4, Timestamp.valueOf(departureDateTime));

                pst.setInt(5, Integer.parseInt(seatsField.getText()));
                pst.setDouble(6, Double.parseDouble(priceField.getText()));
                pst.setString(7, (String) busIdCombo.getSelectedItem());

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Bus updated successfully!");
                dispose();
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error updating bus: " + ex.getMessage());
        }
    }
}

class DeleteBusInternalFrame extends JInternalFrame {
    private JComboBox<String> busIdCombo;
    private JTextArea busDetails;

    public DeleteBusInternalFrame() {
        super("Delete Bus", true, true, true, true);
        initComponents();
        setSize(400, 300);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Bus ID:"));
        busIdCombo = new JComboBox<>();
        loadBusIds();
        topPanel.add(busIdCombo);
        add(topPanel, BorderLayout.NORTH);

        busDetails = new JTextArea(10, 40);
        busDetails.setEditable(false);
        add(new JScrollPane(busDetails), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete Bus");
        deleteButton.addActionListener(e -> deleteBus());
        add(deleteButton, BorderLayout.SOUTH);

        busIdCombo.addActionListener(e -> showBusDetails());
        BusFileManager.logAction("Deleted bus: " + busIdCombo.getSelectedItem());
    }

    private void loadBusIds() {
        try {
            Connection conn = ConnectionProvider.getCon();
            String sql = "SELECT busID FROM buses";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    busIdCombo.addItem(rs.getString("busID"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bus IDs: " + ex.getMessage());
        }
    }

    private void showBusDetails() {
        String selectedBusId = (String) busIdCombo.getSelectedItem();
        if (selectedBusId == null) return;

        try {
            Connection conn = ConnectionProvider.getCon();
            String sql = "SELECT * FROM buses WHERE busID = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, selectedBusId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        StringBuilder details = new StringBuilder();
                        details.append("Bus ID: ").append(rs.getString("busID")).append("\n");
                        details.append("Source: ").append(rs.getString("source")).append("\n");
                        details.append("Destination: ").append(rs.getString("destination")).append("\n");
                        details.append("Departure Date: ").append(rs.getDate("departureDate")).append("\n");
                        details.append("Departure Time: ").append(rs.getTime("departureTime")).append("\n");
                        details.append("Total Seats: ").append(rs.getInt("totalSeats")).append("\n");
                        details.append("Ticket Price: RM").append(rs.getDouble("ticketPrice"));
                        busDetails.setText(details.toString());
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bus details: " + ex.getMessage());
        }
    }

    private void deleteBus() {
        String selectedBusId = (String) busIdCombo.getSelectedItem();
        if (selectedBusId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this bus?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = ConnectionProvider.getCon();
                String sql = "DELETE FROM buses WHERE busID = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, selectedBusId);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Bus deleted successfully!");
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting bus: " + ex.getMessage());
            }
        }
    }
}

// Database connection provider class
class ConnectionProvider {
    private static final String URL = "jdbc:mysql://localhost:3306/busbooking";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getCon() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}