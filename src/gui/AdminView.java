package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.text.SimpleDateFormat;

public class AdminView extends JInternalFrame {
    private JTable table;
    private JTextField searchField;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public AdminView() {
        super("View All Bookings", true, true, true, true);
        initComponents();
        setSize(1000, 600);
        setVisible(true);
    }

    private void initComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchBookings();
            }
        });
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBookingData());
        searchPanel.add(refreshButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Table setup
        JScrollPane scrollPane = new JScrollPane();
        table = new JTable();
        setupTable();
        loadBookingData();
        scrollPane.setViewportView(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Status update panel at the bottom
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cancelButton = new JButton("Cancel Selected Booking");
        cancelButton.addActionListener(e -> cancelSelectedBooking());
        statusPanel.add(cancelButton);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void setupTable() {
        String[] columns = {
            "Booking ID", "Bus ID", "User ID", "Customer Name", "Source", "Destination",
            "Departure Date", "Departure Time", "Seat Number", "Ticket Price",
            "Booking Date/Time", "Status"
        };
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 8) return Integer.class; // Seat Number
                if (columnIndex == 9) return Double.class;  // Ticket Price
                return String.class;
            }
        };
        
        table.setModel(model);
        
        // Setup sorter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150);  // Booking ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80);   // Bus ID
        table.getColumnModel().getColumn(2).setPreferredWidth(60);   // User ID
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // Customer Name
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Source
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Destination
    }

    private void loadBookingData() {
        model.setRowCount(0); // Clear existing data
        
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT b.*, bu.source, bu.destination, bu.departureDate, bu.departureTime, " +
                 "u.username as customerName " +
                 "FROM booking b " +
                 "JOIN buses bu ON b.busID = bu.busID " +
                 "JOIN users u ON b.userID = u.userID " +
                 "ORDER BY b.bookingDateTime DESC")) {
            
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("bookingID"),
                    rs.getString("busID"),
                    rs.getInt("userID"),
                    rs.getString("customerName"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    dateFormat.format(rs.getDate("departureDate")),
                    timeFormat.format(rs.getTimestamp("departureTime")),
                    rs.getInt("seatNumber"),
                    rs.getDouble("ticketPrice"),
                    dateTimeFormat.format(rs.getTimestamp("bookingDateTime")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading booking data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBookings() {
        String searchText = searchField.getText().toLowerCase();
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText);
        sorter.setRowFilter(rf);
    }

    private void cancelSelectedBooking() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a booking to cancel",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the actual row index if table is sorted
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String bookingID = (String) model.getValueAt(modelRow, 0);
        String currentStatus = (String) model.getValueAt(modelRow, 11);

        if ("CANCELLED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "This booking is already cancelled",
                "Cannot Cancel",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking " + bookingID + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = ConnectionProvider.getCon();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE booking SET status = 'CANCELLED' WHERE bookingID = ?")) {
                
                ps.setString(1, bookingID);
                int result = ps.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Booking cancelled successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadBookingData(); // Refresh the table
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error cancelling booking: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}