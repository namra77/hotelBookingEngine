package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.service.BookingService;
import com.example.hotelbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        try {
            Booking booking = bookingService.createBooking(
                bookingRequest.getUserId(),
                bookingRequest.getRoomId(),
                bookingRequest.getCheckIn(),
                bookingRequest.getCheckOut(),
                bookingRequest.getPaymentMethod()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("booking", booking);
            response.put("numberOfNights", bookingRequest.getNumberOfNights());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get current user's bookings (NEW ENDPOINT TO MATCH SECURITY CONFIG)
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail);

            if (size > 0) {
                // Paginated response
                Pageable pageable = PageRequest.of(page, size);
                Page<Booking> bookings = bookingService.getBookingsByUserId(currentUser.getId(), pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("bookings", bookings.getContent());
                response.put("totalElements", bookings.getTotalElements());
                response.put("totalPages", bookings.getTotalPages());
                response.put("currentPage", bookings.getNumber());
                response.put("userId", currentUser.getId());

                return ResponseEntity.ok(response);
            } else {
                // Non-paginated response
                List<Booking> bookings = bookingService.getBookingsByUserId(currentUser.getId());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("bookings", bookings);
                response.put("userId", currentUser.getId());

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel current user's booking (NEW ENDPOINT FOR CONVENIENCE)
     */
    @DeleteMapping("/my/{id}")
    public ResponseEntity<?> cancelMyBooking(@PathVariable Long id) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail);

            bookingService.cancelBooking(id, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get all bookings (Admin only)
     */
    @GetMapping
    public ResponseEntity<?> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Booking> bookings = bookingService.getAllBookings(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookings", bookings.getContent());
            response.put("totalElements", bookings.getTotalElements());
            response.put("totalPages", bookings.getTotalPages());
            response.put("currentPage", bookings.getNumber());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get user's bookings by user ID (Admin can access any user, customers need proper authorization)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Additional security check for customers
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail);
            
            // Customers can only access their own bookings
            if (currentUser.getRole().name().equals("CUSTOMER") && !currentUser.getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Access denied: You can only view your own bookings");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            if (size > 0) {
                // Paginated response
                Pageable pageable = PageRequest.of(page, size);
                Page<Booking> bookings = bookingService.getBookingsByUserId(userId, pageable);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("bookings", bookings.getContent());
                response.put("totalElements", bookings.getTotalElements());
                response.put("totalPages", bookings.getTotalPages());
                response.put("currentPage", bookings.getNumber());

                return ResponseEntity.ok(response);
            } else {
                // Non-paginated response
                List<Booking> bookings = bookingService.getBookingsByUserId(userId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("bookings", bookings);

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel a booking
     */
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @PathVariable Long userId) {
        try {
            // Additional security check for customers
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail);
            
            // Customers can only cancel their own bookings
            if (currentUser.getRole().name().equals("CUSTOMER") && !currentUser.getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Access denied: You can only cancel your own bookings");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            bookingService.cancelBooking(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Admin cancel booking (without user validation)
     */
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<?> adminCancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking cancelled by admin successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get available rooms for date range (ENHANCED WITH ACTUAL AVAILABILITY LOGIC)
     */
    @GetMapping("/available-rooms")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            // Use the booking service to get truly available rooms
            List<Room> availableRooms = bookingService.getAvailableRooms(checkInDate, checkOutDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("availableRooms", availableRooms);
            response.put("checkIn", checkInDate);
            response.put("checkOut", checkOutDate);
            response.put("numberOfNights", java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate));
            response.put("totalRoomsAvailable", availableRooms.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Check room availability and get price
     */
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkRoomAvailability(
            @RequestParam Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            boolean available = bookingService.isRoomAvailable(roomId, checkInDate, checkOutDate);
            double totalAmount = 0.0;
            
            if (available) {
                totalAmount = bookingService.calculateTotalAmount(roomId, checkInDate, checkOutDate);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", available);
            response.put("totalAmount", totalAmount);
            response.put("roomId", roomId);
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

    /**
     * Validate booking request (utility endpoint)
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateBookingRequest(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        try {
            // The validation is handled by @Valid annotation
            // If we reach here, the validation passed
            
            // Additional business validation
            boolean roomAvailable = bookingService.isRoomAvailable(
                bookingRequest.getRoomId(), 
                bookingRequest.getCheckIn(), 
                bookingRequest.getCheckOut()
            );
            
            double totalAmount = bookingService.calculateTotalAmount(
                bookingRequest.getRoomId(), 
                bookingRequest.getCheckIn(), 
                bookingRequest.getCheckOut()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", roomAvailable);
            response.put("totalAmount", totalAmount);
            response.put("numberOfNights", bookingRequest.getNumberOfNights());
            response.put("message", roomAvailable ? "Booking request is valid" : "Room is not available for selected dates");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("valid", false);
//            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}