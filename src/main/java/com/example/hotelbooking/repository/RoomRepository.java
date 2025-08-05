package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Find room by room number
    Optional<Room> findByRoomNumber(String roomNumber);
    
    // Find available rooms
    List<Room> findByAvailableTrue();
    
    // Find rooms by type
    List<Room> findByTypeIgnoreCase(String type);
    
    // Find rooms by type and availability
    List<Room> findByTypeIgnoreCaseAndAvailable(String type, boolean available);
    
    // Find rooms by capacity
    List<Room> findByCapacityGreaterThanEqual(int capacity);
    
    // Find rooms within price range
    List<Room> findByPricePerNightBetween(double minPrice, double maxPrice);
    
    // Find rooms with discount
    @Query("SELECT r FROM Room r WHERE r.discountPercentage > 0 AND r.available = true")
    List<Room> findRoomsWithDiscount();
    
    // Find available rooms for specific dates (no conflicting bookings)
    @Query("SELECT r FROM Room r WHERE r.available = true AND r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'PENDING') AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)))")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                 @Param("checkOut") LocalDate checkOut);
    
    // Find available rooms by type for specific dates
    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "LOWER(r.type) = LOWER(:type) AND r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'PENDING') AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)))")
    List<Room> findAvailableRoomsByType(@Param("checkIn") LocalDate checkIn,
                                       @Param("checkOut") LocalDate checkOut,
                                       @Param("type") String type);
    
    // Find available rooms with capacity for specific dates
    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "r.capacity >= :capacity AND r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'PENDING') AND " +
           "((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)))")
    List<Room> findAvailableRoomsByCapacity(@Param("checkIn") LocalDate checkIn,
                                           @Param("checkOut") LocalDate checkOut,
                                           @Param("capacity") int capacity);
    
    // Search rooms by description or type
    @Query("SELECT r FROM Room r WHERE " +
           "LOWER(r.type) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "r.roomNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Room> searchRooms(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find rooms ordered by price
    List<Room> findByAvailableTrueOrderByPricePerNightAsc();
    List<Room> findByAvailableTrueOrderByPricePerNightDesc();
    
    // Count rooms by type
    long countByTypeIgnoreCase(String type);
    
    // Count available rooms
    long countByAvailableTrue();
}
