package com.example.hotelbooking.service;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.PaymentMethod;
import com.example.hotelbooking.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /**
     * Creates a new booking
     */
    Booking createBooking(Long userId, Long roomId, LocalDate checkIn, 
                         LocalDate checkOut, PaymentMethod paymentMethod);

    /**
     * Retrieves all bookings
     */
    List<Booking> getAllBookings();

    /**
     * Retrieves all bookings with pagination
     */
    Page<Booking> getAllBookings(Pageable pageable);

    /**
     * Retrieves a booking by ID
     */
    Booking getBookingById(Long id);

    /**
     * Retrieves bookings by user ID
     */
    List<Booking> getBookingsByUserId(Long userId);

    /**
     * Retrieves bookings by user ID with pagination
     */
    Page<Booking> getBookingsByUserId(Long userId, Pageable pageable);

    /**
     * Cancels a booking with user authorization
     */
    void cancelBooking(Long bookingId, Long userId);

    /**
     * Cancels a booking (admin method)
     */
    void cancelBooking(Long bookingId);

    /**
     * Gets available rooms for date range
     */
    List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    /**
     * Checks if room is available
     */
    boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Calculates total amount for booking
     */
    double calculateTotalAmount(Long roomId, LocalDate checkIn, LocalDate checkOut);
}