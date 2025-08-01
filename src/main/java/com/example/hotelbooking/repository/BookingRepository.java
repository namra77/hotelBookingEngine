package com.example.hotelbooking.repository;


import com.example.hotelbooking.model.Booking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	boolean existsByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
	        Long roomId, LocalDate checkOutDate, LocalDate checkInDate);
	
	List<Booking> findByRoomId(Long roomId);
	
//tells Spring Data JPA to return all bookings made by a specific user.
	List<Booking> findByUserId(Long userId);
}
