package gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BusUser {
    private final String busID;
    private final String source;
    private final String destination;
    private final LocalDate departureDate;
    private final LocalDateTime departureTime;
    private final int totalSeats;
    private final double ticketPrice;

    public BusUser(String busID, String source, String destination, 
                   LocalDate departureDate, LocalDateTime departureTime, 
                   int totalSeats, double ticketPrice) {
        this.busID = busID;
        this.source = source;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.ticketPrice = ticketPrice;
    }

    public String getBusID() {
        return busID;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public String getBusInfo(int bookedSeatsCount) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("%s - %s | Seats Available: %d/%d | Departure: %s %s | RM%.2f", 
            source, 
            destination, 
            (totalSeats - bookedSeatsCount),
            totalSeats,
            departureDate,
            departureTime.format(timeFormatter),
            ticketPrice
        );
    }

    // Overloaded method for backward compatibility
    public String getBusInfo() {
        return getBusInfo(0);
    }
}