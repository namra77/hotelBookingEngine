package com.example.hotelbooking.service;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.BookingStatus;
import com.example.hotelbooking.model.PaymentMethod;
import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.RoomRepository;
import com.example.hotelbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    // ====== Core Booking Operations ======

    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate, PaymentMethod paymentMethod) {
        log.info("Creating booking for user {} in room {} from {} to {}", userId, roomId, checkInDate, checkOutDate);

        // Validate input parameters
        validateBookingInput(userId, roomId, checkInDate, checkOutDate, paymentMethod);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // Validate booking dates
        validateBookingDates(checkInDate, checkOutDate);

        // Check room availability
        if (!isRoomAvailable(roomId, checkInDate, checkOutDate)) {
            throw new RuntimeException("Room is not available for the selected dates");
        }

        // Check if room is generally available
        if (!room.isAvailable()) {
            throw new RuntimeException("Room is currently not available for booking");
        }

        // Calculate pricing
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double basePrice = room.getPricePerNight();
        double discountedPrice = basePrice * (1 - room.getDiscountPercentage() / 100.0);
        double totalAmount = nights * discountedPrice;

        log.info("Booking calculation: {} nights × ${} ({}% discount) = ${}", 
                nights, basePrice, room.getDiscountPercentage(), totalAmount);

        // Process payment
        processPayment(user, totalAmount, paymentMethod);

        // Create and save booking
        Booking booking = createBookingEntity(user, room, checkInDate, checkOutDate, totalAmount, paymentMethod);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created successfully with ID: {}", savedBooking.getId());
        return savedBooking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking with ID: {}", bookingId);
        
        Booking booking = getBookingById(bookingId);
        
        // Validate cancellation eligibility
        if (!canCancelBooking(bookingId, booking.getUser().getId())) {
            throw new RuntimeException("This booking cannot be cancelled");
        }
        
        // Process refund if applicable
        processRefund(booking);
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        log.info("Booking {} cancelled successfully", bookingId);
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = getBookingById(bookingId);
        BookingStatus oldStatus = booking.getStatus();
        
        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);
        
        log.info("Booking {} status updated from {} to {}", bookingId, oldStatus, newStatus);
        return updatedBooking;
    }

    // ====== Query Operations ======

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findAll().stream()
                .filter(booking -> 
                    !booking.getCheckInDate().isAfter(endDate) && 
                    !booking.getCheckOutDate().isBefore(startDate))
                .collect(Collectors.toList());
    }

    public List<Booking> getUpcomingBookings() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getCheckInDate().isAfter(today) || 
                                 booking.getCheckInDate().equals(today))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());
    }

    public List<Booking> getCurrentBookings() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(booking -> 
                    !booking.getCheckInDate().isAfter(today) && 
                    booking.getCheckOutDate().isAfter(today))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());
    }

    // ====== Search and Filter Operations ======

    public List<Booking> searchBookings(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllBookings();
        }
        
        String lowercaseSearchTerm = searchTerm.toLowerCase().trim();
        
        return bookingRepository.findAll().stream()
                .filter(booking -> 
                    booking.getUser().getFullName().toLowerCase().contains(lowercaseSearchTerm) ||
                    booking.getUser().getEmail().toLowerCase().contains(lowercaseSearchTerm) ||
                    booking.getRoom().getRoomNumber().toLowerCase().contains(lowercaseSearchTerm) ||
                    booking.getRoom().getType().toLowerCase().contains(lowercaseSearchTerm) ||
                    booking.getId().toString().contains(lowercaseSearchTerm))
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsForManager(String managerEmail) {
        // For now, return all bookings. You can customize this based on hotel/manager relationships
        return getAllBookings();
    }

    // ====== Room Availability Operations ======

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Use the more efficient repository method
        boolean hasConflict = bookingRepository.existsByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                roomId, checkOut, checkIn);
        
        if (hasConflict) {
            // Double-check with detailed logic for edge cases
            List<Booking> existingBookings = bookingRepository.findByRoomId(roomId);
            return existingBookings.stream()
                    .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                    .noneMatch(booking -> 
                        booking.getCheckInDate().isBefore(checkOut) && 
                        checkIn.isBefore(booking.getCheckOutDate()));
        }
        
        return true;
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> room.isAvailable())
                .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
                .collect(Collectors.toList());
    }

    // ====== Validation and Business Logic ======

    public boolean canCancelBooking(Long bookingId, Long userId) {
        try {
            Booking booking = getBookingById(bookingId);
            
            // Check ownership
            if (!booking.getUser().getId().equals(userId)) {
                return false;
            }
            
            // Check status
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                return false;
            }
            
            // Check if booking is in the future (allow cancellation on check-in day)
            return !booking.getCheckInDate().isBefore(LocalDate.now());
            
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean canModifyBooking(Long bookingId, Long userId) {
        try {
            Booking booking = getBookingById(bookingId);
            
            // Check ownership
            if (!booking.getUser().getId().equals(userId)) {
                return false;
            }
            
            // Check status
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                return false;
            }
            
            // Allow modification until check-in date
            return booking.getCheckInDate().isAfter(LocalDate.now());
            
        } catch (RuntimeException e) {
            return false;
        }
    }



    // ====== Private Helper Methods ======

    private void validateBookingInput(Long userId, Long roomId, LocalDate checkIn, LocalDate checkOut, PaymentMethod paymentMethod) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }
        if (checkIn == null) {
            throw new RuntimeException("Check-in date is required");
        }
        if (checkOut == null) {
            throw new RuntimeException("Check-out date is required");
        }
        if (paymentMethod == null) {
            throw new RuntimeException("Payment method is required");
        }
    }

    private void validateBookingDates(LocalDate checkInDate, LocalDate checkOutDate) {
        LocalDate today = LocalDate.now();
        
        if (checkInDate.isBefore(today)) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new RuntimeException("Check-in date must be before check-out date");
        }
        
        long daysBetween = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (daysBetween > 30) {
            throw new RuntimeException("Booking duration cannot exceed 30 days");
        }
    }

    private void processPayment(User user, double totalAmount, PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.ACCOUNT_BALANCE) {
            if (user.getAccountBalance() < totalAmount) {
                throw new RuntimeException(String.format(
                    "Insufficient account balance. Required: $%.2f, Available: $%.2f", 
                    totalAmount, user.getAccountBalance()));
            }
            
            user.setAccountBalance(user.getAccountBalance() - totalAmount);
            userRepository.save(user);
            log.info("Deducted ${} from user {} account balance", totalAmount, user.getId());
            
        } else if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            boolean paymentSuccess = simulateCardPayment(totalAmount);
            if (!paymentSuccess) {
                throw new RuntimeException("Credit card payment failed. Please try again or use a different payment method.");
            }
            log.info("Credit card payment of ${} processed successfully", totalAmount);
        }
    }

    private void processRefund(Booking booking) {
        if (booking.getPaymentMethod() == PaymentMethod.ACCOUNT_BALANCE) {
            User user = booking.getUser();
            user.setAccountBalance(user.getAccountBalance() + booking.getTotalPrice());
            userRepository.save(user);
            log.info("Refunded ${} to user {} account balance", booking.getTotalPrice(), user.getId());
        } else if (booking.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            // In a real application, you would integrate with payment gateway for refunds
            log.info("Credit card refund of ${} initiated for booking {}", booking.getTotalPrice(), booking.getId());
        }
    }

    private Booking createBookingEntity(User user, Room room, LocalDate checkIn, LocalDate checkOut, double totalAmount, PaymentMethod paymentMethod) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setTotalPrice(totalAmount);
        booking.setPaymentMethod(paymentMethod);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        
        return booking;
    }

    private boolean simulateCardPayment(double amount) {
        // Simulate payment processing
        try {
            // Simulate network delay
            Thread.sleep(100);
            
            // Simulate 95% success rate
            double random = Math.random();
            boolean success = random < 0.95;
            
            if (success) {
                log.info("Simulated credit card payment successful for amount: ${}", amount);
            } else {
                log.warn("Simulated credit card payment failed for amount: ${}", amount);
            }
            
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
            return false;
        }
    }
}