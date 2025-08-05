package com.example.hotelbooking.controller;

import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.service.BookingService;
import com.example.hotelbooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;
    
    
    @Autowired
    private BookingService bookingService;

    /**
     * Get all rooms
     */
    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rooms", rooms);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get room by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("room", room);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

   
    /**
     * Get available rooms 
     * If no dates provided, returns all rooms with their general availability status
     * If dates provided, returns rooms available for those specific dates
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut) {
        try {
            List<Room> availableRooms;
            Map<String, Object> response = new HashMap<>();
            
            if (checkIn != null && checkOut != null) {
                // Get rooms available for specific dates
                LocalDate checkInDate = LocalDate.parse(checkIn);
                LocalDate checkOutDate = LocalDate.parse(checkOut);
                
                // Validate date range
                if (!checkInDate.isBefore(checkOutDate)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Check-out date must be after check-in date");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                
                if (checkInDate.isBefore(LocalDate.now())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Check-in date cannot be in the past");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                
                availableRooms = bookingService.getAvailableRooms(checkInDate, checkOutDate);
                
                response.put("success", true);
                response.put("availableRooms", availableRooms);
                response.put("checkIn", checkInDate);
                response.put("checkOut", checkOutDate);
                response.put("numberOfNights", java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate));
                response.put("totalAvailableRooms", availableRooms.size());
                response.put("searchType", "date_specific");
                
            } else {
                // Get all rooms (general availability - room exists and is not disabled)
                availableRooms = roomService.getAllRooms();
                
                response.put("success", true);
                response.put("availableRooms", availableRooms);
                response.put("totalAvailableRooms", availableRooms.size());
                response.put("searchType", "general");
                response.put("message", "All rooms listed. For date-specific availability, provide checkIn and checkOut parameters.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get room availability status for specific dates
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getRoomAvailability(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            // Get the room first to ensure it exists
            Room room = roomService.getRoomById(id);
            
            // Check availability
            boolean available = bookingService.isRoomAvailable(id, checkInDate, checkOutDate);
            double totalAmount = 0.0;
            
            if (available) {
                totalAmount = bookingService.calculateTotalAmount(id, checkInDate, checkOutDate);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("room", room);
            response.put("available", available);
            response.put("totalAmount", totalAmount);
            response.put("pricePerNight", room.getPricePerNight());
            response.put("checkIn", checkInDate);
            response.put("checkOut", checkOutDate);
            response.put("numberOfNights", java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

