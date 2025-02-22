package gui;

import com.toedter.calendar.JDateChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import dao.ConnectionProvider;

public class BookingForm extends JFrame implements ActionListener {
    private JLabel BF_Title, CustomerName, Source, Destination, DepartureDate, DepartureTime, SeatNumber, Price;
    private JTextField tf_cust_name;
    private JComboBox<String> source_list, destination_list;
    private JPanel p;
    private JButton btn_book, btn_select_seat;
    private String userID;
    private BusUser bus;
    private JDateChooser dateChooser;
    private String bookingID;
    private int selectedSeat = -1;
    private JLabel selectedSeatLabel;

    public BookingForm(String userID, BusUser bus) {
        super("Bus Ticket Booking");
        
        this.userID = userID;
        this.bus = bus;
        
        // Use a main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form panel with GridBagLayout
        p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        initializeComponents();
        setupLayout();
        
        // Add components to main panel
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(p, BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        BF_Title = new JLabel("Bus Ticket Booking");
        BF_Title.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(BF_Title);
        
        return headerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btn_book = new JButton("BOOK TICKET");
        btn_book.setFont(new Font("Arial", Font.BOLD, 14));
        btn_book.setPreferredSize(new Dimension(150, 40));
        btn_book.addActionListener(this);
        btn_book.setEnabled(false);
        
        buttonPanel.add(btn_book);
        return buttonPanel;
    }

    private void initializeComponents() {
        // Standard label width for alignment
        int labelWidth = 120;
        
        CustomerName = createLabel("Name:", labelWidth);
        tf_cust_name = new JTextField(20);
        styleTextField(tf_cust_name);

        Source = createLabel("From:", labelWidth);
        source_list = new JComboBox<>(new String[] {bus.getSource()});
        styleComboBox(source_list);
        source_list.setEnabled(false);

        Destination = createLabel("To:", labelWidth);
        destination_list = new JComboBox<>(new String[] {bus.getDestination()});
        styleComboBox(destination_list);
        destination_list.setEnabled(false);

        SeatNumber = createLabel("Selected Seat:", labelWidth);
        selectedSeatLabel = new JLabel("None");
        selectedSeatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        btn_select_seat = new JButton("Select Seat");
        styleButton(btn_select_seat);
        btn_select_seat.addActionListener(e -> showSeatSelectionDialog());

        DepartureDate = createLabel("Departure Date:", labelWidth);
        dateChooser = new JDateChooser();
        dateChooser.setDate(java.sql.Date.valueOf(bus.getDepartureDate()));
        dateChooser.setEnabled(false);
        styleDateChooser(dateChooser);

        DepartureTime = createLabel("Departure Time:", labelWidth);
        
        Price = createLabel(String.format("Price: RM %.2f", bus.getTicketPrice()), labelWidth);
        Price.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private JLabel createLabel(String text, int width) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(width, 25));
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(200, 25));
        textField.setBorder(BorderFactory.createCompoundBorder(
            textField.getBorder(),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(200, 25));
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 25));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void styleDateChooser(JDateChooser dateChooser) {
        dateChooser.setPreferredSize(new Dimension(200, 25));
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        
        // Customer Name
        addFormRow(CustomerName, tf_cust_name, 0);

        // Source
        addFormRow(Source, source_list, 1);

        // Destination
        addFormRow(Destination, destination_list, 2);

        // Seat Selection
        JPanel seatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        seatPanel.add(selectedSeatLabel);
        seatPanel.add(Box.createHorizontalStrut(10));
        seatPanel.add(btn_select_seat);
        addFormRow(SeatNumber, seatPanel, 3);

        // Departure Date
        addFormRow(DepartureDate, dateChooser, 4);

        // Departure Time
        JLabel timeLabel = new JLabel(bus.getDepartureTime().toLocalTime().toString());
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        addFormRow(DepartureTime, timeLabel, 5);

        // Price (centered)
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        p.add(Price, gbc);
    }

    private void addFormRow(JLabel label, Component component, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        p.add(label, gbc);
        
        // Component
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(component, gbc);
    }

    private void showSeatSelectionDialog() {
        JDialog dialog = new JDialog(this, "Select a Seat", true);
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(0, 4, 5, 5));
        seatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        java.util.List<Integer> bookedSeats = getBookedSeats();

        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            JButton seatButton = new JButton(String.valueOf(i));
            seatButton.setPreferredSize(new Dimension(60, 40));
            
            if (bookedSeats.contains(i)) {
                seatButton.setEnabled(false);
                seatButton.setBackground(Color.RED);
                seatButton.setToolTipText("Seat " + i + " is already booked");
            } else {
                seatButton.setBackground(Color.GREEN);
                final int seatNum = i;
                seatButton.addActionListener(e -> {
                    selectedSeat = seatNum;
                    selectedSeatLabel.setText(String.valueOf(seatNum));
                    btn_book.setEnabled(true);
                    dialog.dispose();
                });
            }
            seatPanel.add(seatButton);
        }

        // Add legend
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel availableLabel = new JLabel("Available");
        availableLabel.setForeground(Color.GREEN);
        JLabel bookedLabel = new JLabel("Booked");
        bookedLabel.setForeground(Color.RED);
        
        legendPanel.add(availableLabel);
        legendPanel.add(new JLabel(" | "));
        legendPanel.add(bookedLabel);

        dialog.setLayout(new BorderLayout());
        dialog.add(seatPanel, BorderLayout.CENTER);
        dialog.add(legendPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private java.util.List<Integer> getBookedSeats() {
        java.util.List<Integer> bookedSeats = new java.util.ArrayList<>();
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT SeatNumber FROM booking WHERE busID = ? AND status = 'CONFIRMED'")) {
            
            ps.setString(1, bus.getBusID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookedSeats.add(rs.getInt("SeatNumber"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading booked seats: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return bookedSeats;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btn_book) {
            if (tf_cust_name.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter customer name.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (selectedSeat == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a seat.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = ConnectionProvider.getCon();
                 PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO BOOKING(bookingID, busID, userID, CustomerName, SeatNumber, ticketPrice, status) " +
                     "VALUES(?,?,?,?,?,?,'CONFIRMED')")) {
                
                String bookingID = generateBookingID();
                ps.setString(1, bookingID);
                ps.setString(2, bus.getBusID());
                ps.setString(3, userID);
                ps.setString(4, tf_cust_name.getText().trim());
                ps.setInt(5, selectedSeat);
                ps.setDouble(6, bus.getTicketPrice());

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, 
                    "Ticket booked successfully!\nBooking ID: " + bookingID, 
                    "Booking Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error booking ticket: " + ex.getMessage(), 
                    "Booking Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String generateBookingID() {
        return "BK" + System.currentTimeMillis();
    }
}