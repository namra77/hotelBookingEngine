package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find bookings by user ID with ordering
    List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);
    
    // Find bookings by user ID with pagination
    Page<Booking> findByUserIdOrderByBookingDateDesc(Long userId, Pageable pageable);
    
    // Find bookings by user ID and status
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    
    // Find bookings by user ID and status with pagination
    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);
    
    // Count bookings by user ID and status
    long countByUserIdAndStatus(Long userId, BookingStatus status);
    
    // Find conflicting bookings for a room in a date range
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND " +
           "b.status = :status AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut,
                                         @Param("status") BookingStatus status);
    
    // Find all conflicting bookings (multiple statuses)
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND " +
           "b.status IN ('CONFIRMED', 'PENDING') AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Booking> findConflictingActiveBookings(@Param("roomId") Long roomId,
                                               @Param("checkIn") LocalDate checkIn,
                                               @Param("checkOut") LocalDate checkOut);
    
    // Find bookings by room ID
    List<Booking> findByRoomIdOrderByCheckInDateDesc(Long roomId);
    
    // Find bookings within date range
    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :startDate AND b.checkInDate <= :endDate")
    List<Booking> findBookingsByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    // Find bookings by status with pagination
    Page<Booking> findByStatusOrderByBookingDateDesc(BookingStatus status, Pageable pageable);
    
    // Search bookings by user name or email
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Booking> searchBookingsByUser(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find upcoming bookings (check-in date is today or later)
    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :today AND b.status = 'CONFIRMED'")
    List<Booking> findUpcomingBookings(@Param("today") LocalDate today);
    
    // Find current active bookings (guest is currently staying)
    @Query("SELECT b FROM Booking b WHERE :today >= b.checkInDate AND :today < b.checkOutDate AND b.status = 'CONFIRMED'")
    List<Booking> findCurrentActiveBookings(@Param("today") LocalDate today);
    
    // Revenue calculation query
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'CONFIRMED' AND " +
           "b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Double calculateRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
