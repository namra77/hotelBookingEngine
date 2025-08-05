package com.example.hotelbooking.service;

import com.example.hotelbooking.exception.*;
import com.example.hotelbooking.model.*;
import com.example.hotelbooking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new booking with proper concurrency control and validation
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public Booking createBooking(Long userId, Long roomId, LocalDate checkIn, 
                               LocalDate checkOut, PaymentMethod paymentMethod) {
        
        logger.info("Creating booking for user {} and room {} from {} to {}", 
                   userId, roomId, checkIn, checkOut);
        
        // Input validation
        validateBookingInput(userId, roomId, checkIn, checkOut, paymentMethod);
        
        // Fetch entities with pessimistic locking to prevent concurrent modifications
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        Room room = entityManager.find(Room.class, roomId, LockModeType.PESSIMISTIC_WRITE);
        if (room == null) {
            throw new RoomNotFoundException("Room not found with id: " + roomId);
        }
        
        // Business rule validations
        validateBookingRules(user, room, checkIn, checkOut);
        
        // Check for booking conflicts with locking
        if (hasBookingConflict(roomId, checkIn, checkOut)) {
            throw new BookingConflictException(
                String.format("Room %s is not available from %s to %s", 
                             room.getRoomNumber(), checkIn, checkOut));
        }
        
        // Calculate pricing
        BigDecimal totalPrice = calculateTotalPrice(room, checkIn, checkOut);
        
        // Process payment
        processPayment(user, totalPrice, paymentMethod);
        
        // Create and save booking
        Booking booking = createBookingEntity(user, room, checkIn, checkOut, 
                                            paymentMethod, totalPrice);
        
        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Successfully created booking with id: {}", savedBooking.getId());
        
        return savedBooking;
    }

    /**
     * Validates input parameters
     */
    private void validateBookingInput(Long userId, Long roomId, LocalDate checkIn, 
                                    LocalDate checkOut, PaymentMethod paymentMethod) {
        if (userId == null) {
            throw new InvalidBookingDataException("User ID cannot be null");
        }
        if (roomId == null) {
            throw new InvalidBookingDataException("Room ID cannot be null");
        }
        if (checkIn == null) {
            throw new InvalidBookingDataException("Check-in date cannot be null");
        }
        if (checkOut == null) {
            throw new InvalidBookingDataException("Check-out date cannot be null");
        }
        if (paymentMethod == null) {
            throw new InvalidBookingDataException("Payment method cannot be null");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingDataException("Check-in date cannot be in the past");
        }
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            throw new InvalidBookingDataException("Check-out must be at least one day after check-in");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 30) {
            throw new InvalidBookingDataException("Booking cannot exceed 30 days");
        }
    }

    /**
     * Validates business rules
     */
    private void validateBookingRules(User user, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is not available");
        }
        
        if (user.getRole() != Role.CUSTOMER) {
            throw new UnauthorizedBookingException("Only customers can make bookings");
        }
        
        // Check if user has any active bookings (optional business rule)
        long activeBookings = bookingRepository.countByUserIdAndStatus(user.getId(), BookingStatus.CONFIRMED);
        if (activeBookings >= 3) { // Max 3 active bookings per user
            throw new BookingLimitExceededException("User has reached maximum active bookings limit");
        }
    }

    /**
     * Checks for booking conflicts using database-level locking
     */
    private boolean hasBookingConflict(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
            roomId, checkIn, checkOut, BookingStatus.CONFIRMED);
        return !conflictingBookings.isEmpty();
    }

    /**
     * Calculates total price with discounts
     */
    private BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal basePrice = BigDecimal.valueOf(room.getPricePerNight())
                                        .multiply(BigDecimal.valueOf(numberOfNights));
        
        // Apply discount if available
        if (room.getDiscountPercentage() > 0) {
            BigDecimal discountAmount = basePrice
                .multiply(BigDecimal.valueOf(room.getDiscountPercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            basePrice = basePrice.subtract(discountAmount);
        }
        
        // Apply seasonal pricing (example business rule)
        if (isHighSeason(checkIn, checkOut)) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.2)); // 20% surcharge
        }
        
        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Determines if dates fall in high season
     */
    private boolean isHighSeason(LocalDate checkIn, LocalDate checkOut) {
        // Example: December and January are high season
        return (checkIn.getMonthValue() == 12 || checkIn.getMonthValue() == 1) ||
               (checkOut.getMonthValue() == 12 || checkOut.getMonthValue() == 1);
    }

    /**
     * Processes payment based on payment method
     */
    private void processPayment(User user, BigDecimal amount, PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case ACCOUNT_BALANCE:
                processAccountBalancePayment(user, amount);
                break;
            case CREDIT_CARD:
                processCreditCardPayment(user, amount);
                break;
            default:
                throw new UnsupportedPaymentMethodException("Payment method not supported: " + paymentMethod);
        }
    }

    /**
     * Processes payment via account balance
     */
    private void processAccountBalancePayment(User user, BigDecimal amount) {
        BigDecimal currentBalance = BigDecimal.valueOf(user.getAccountBalance());
        
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                String.format("Insufficient account balance. Required: $%.2f, Available: $%.2f", 
                             amount.doubleValue(), currentBalance.doubleValue()));
        }
        
        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setAccountBalance(newBalance.doubleValue());
        userRepository.save(user);
        
        logger.info("Processed account balance payment of ${} for user {}", amount, user.getId());
    }

    /**
     * Processes credit card payment (stub implementation)
     */
    private void processCreditCardPayment(User user, BigDecimal amount) {
        // In real implementation, integrate with payment gateway
        // For now, just log the transaction
        logger.info("Processing credit card payment of ${} for user {}", amount, user.getId());
        
        // Simulate payment processing
        if (Math.random() < 0.05) { // 5% chance of payment failure
            throw new PaymentProcessingException("Credit card payment failed");
        }
    }

    /**
     * Creates booking entity
     */
    private Booking createBookingEntity(User user, Room room, LocalDate checkIn, 
                                      LocalDate checkOut, PaymentMethod paymentMethod, 
                                      BigDecimal totalPrice) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setTotalPrice(totalPrice.doubleValue());
        booking.setPaymentMethod(paymentMethod);
        booking.setStatus(BookingStatus.CONFIRMED);
        
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId, pageable);
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);
        
        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookingException("User not authorized to cancel this booking");
        }
        
        // Check if cancellation is allowed
        if (booking.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BookingCancellationException("Cannot cancel booking less than 24 hours before check-in");
        }
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingCancellationException("Booking is already cancelled");
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Process refund if payment was via account balance
        if (booking.getPaymentMethod() == PaymentMethod.ACCOUNT_BALANCE) {
            processRefund(booking);
        }
        
        logger.info("Successfully cancelled booking with id: {}", bookingId);
    }

    /**
     * Processes refund for cancelled booking
     */
    private void processRefund(Booking booking) {
        User user = booking.getUser();
        BigDecimal refundAmount = BigDecimal.valueOf(booking.getTotalPrice());
        
        // Apply cancellation fee (example: 10%)
        BigDecimal cancellationFee = refundAmount.multiply(BigDecimal.valueOf(0.1));
        BigDecimal netRefund = refundAmount.subtract(cancellationFee);
        
        BigDecimal newBalance = BigDecimal.valueOf(user.getAccountBalance()).add(netRefund);
        user.setAccountBalance(newBalance.doubleValue());
        userRepository.save(user);
        
        logger.info("Processed refund of ${} (after ${} cancellation fee) for booking {}", 
                   netRefund, cancellationFee, booking.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }

    /**
     * Validates date range for room availability search
     */
    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingDataException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingDataException("Check-in date cannot be in the past");
        }
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            throw new InvalidBookingDataException("Check-out must be at least one day after check-in");
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        
        // Check if cancellation is allowed
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingCancellationException("Booking is already cancelled");
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Process refund if payment was via account balance
        if (booking.getPaymentMethod() == PaymentMethod.ACCOUNT_BALANCE) {
            processRefund(booking);
        }
        
        logger.info("Successfully cancelled booking with id: {}", bookingId);
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));
        
        if (!room.isAvailable()) {
            return false;
        }
        
        return !hasBookingConflict(roomId, checkIn, checkOut);
    }

    @Override
    public double calculateTotalAmount(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));
        
        BigDecimal totalPrice = calculateTotalPrice(room, checkIn, checkOut);
        return totalPrice.doubleValue();
    }
}