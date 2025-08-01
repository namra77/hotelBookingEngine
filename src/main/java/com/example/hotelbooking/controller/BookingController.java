package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequestDTO;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.PaymentMethod;
import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.service.BookingService;
import com.example.hotelbooking.service.RoomService;
import com.example.hotelbooking.service.UserService;
import com.example.hotelbooking.security.CustomUserDetails;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private UserService userService;

    // ====== REST API Endpoints ======
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Booking> createBookingAPI(@RequestBody @Valid BookingRequestDTO request, Authentication authentication) {
        try {
            // Verify the user is booking for themselves
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (!userDetails.getUserId().equals(request.getUserId())) {
                return ResponseEntity.badRequest().build();
            }

            Booking booking = bookingService.createBooking(
                request.getUserId(),
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getPaymentMethod()
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api")
    @ResponseBody
    public List<Booking> getAllBookingsAPI() {
        return bookingService.getAllBookings();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Booking> getBookingByIdAPI(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

//    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
//    @GetMapping("/api/user/{userId}")
//    @ResponseBody
//    public ResponseEntity<List<Booking>> getBookingsByUserAPI(@PathVariable Long userId, Authentication authentication) {
//        // Customers can only view their own bookings, admins can view any
//        if (authentication.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
//            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//            if (!userDetails.getUserId().equals(userId)) {
//                return ResponseEntity.forbidden().build();
//            }
//        }
//        
//        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
//        return ResponseEntity.ok(bookings);
//    }

    // ====== Thymeleaf View Routes ======

    // Show booking form for a specific room
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/book/{roomId}")
    public String showBookingForm(@PathVariable Long roomId, Model model, Authentication authentication) {
        try {
            Room room = roomService.getRoomById(roomId);
            
            // Get the current user's ID
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            
            BookingRequestDTO bookingRequest = new BookingRequestDTO();
            bookingRequest.setRoomId(roomId);
            bookingRequest.setUserId(userId);
            
            model.addAttribute("room", room);
            model.addAttribute("bookingRequest", bookingRequest);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            return "booking/book-room"; // templates/booking/book-room.html
        } catch (RuntimeException e) {
            model.addAttribute("error", "Room not found");
            return "redirect:/rooms/view";
        }
    }

    // Process booking form submission
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDTO request,
                               BindingResult result, Model model, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            try {
                Room room = roomService.getRoomById(request.getRoomId());
                model.addAttribute("room", room);
                model.addAttribute("paymentMethods", PaymentMethod.values());
                model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                return "booking/book-room";
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Room not found");
                return "redirect:/rooms/view";
            }
        }

        try {
            // Verify the user is booking for themselves
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (!userDetails.getUserId().equals(request.getUserId())) {
                throw new RuntimeException("You can only book for yourself");
            }

            Booking booking = bookingService.createBooking(
                request.getUserId(),
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getPaymentMethod()
            );

            redirectAttributes.addFlashAttribute("booking", booking);
            redirectAttributes.addFlashAttribute("success", "Booking created successfully!");
            return "redirect:/bookings/confirmation/" + booking.getId();
            
        } catch (RuntimeException e) {
            try {
                Room room = roomService.getRoomById(request.getRoomId());
                model.addAttribute("room", room);
                model.addAttribute("paymentMethods", PaymentMethod.values());
                model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                model.addAttribute("bookingError", e.getMessage());
                return "booking/book-room";
            } catch (RuntimeException roomError) {
                redirectAttributes.addFlashAttribute("error", "Room not found");
                return "redirect:/rooms/view";
            }
        }
    }

    // Show booking confirmation page
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable Long bookingId, Model model, Authentication authentication) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            
            // Customers can only view their own booking confirmations
            if (authentication.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (!userDetails.getUserId().equals(booking.getUser().getId())) {
                    return "redirect:/customer/dashboard";
                }
            }
            
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            
            model.addAttribute("booking", booking);
            model.addAttribute("nights", nights);
            return "booking/confirmation"; // templates/booking/confirmation.html
        } catch (RuntimeException e) {
            model.addAttribute("error", "Booking not found");
            return "redirect:/customer/dashboard";
        }
    }

    // Admin view - All bookings
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String viewAllBookingsAdmin(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "booking/admin-bookings"; // templates/booking/admin-bookings.html
    }

    // Admin view - Booking details
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public String viewBookingDetailsAdmin(@PathVariable Long id, Model model) {
        try {
            Booking booking = bookingService.getBookingById(id);
            long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            model.addAttribute("booking", booking);
            model.addAttribute("days", days);
            return "booking/booking-details"; // templates/booking/booking-details.html
        } catch (RuntimeException e) {
            model.addAttribute("error", "Booking not found");
            return "redirect:/bookings/admin";
        }
    }

    // Customer view - Their bookings
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-bookings")
    public String viewMyBookings(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getBookingsByUserId(userDetails.getUserId());
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", userDetails.getUser());
        return "booking/my-bookings"; // templates/booking/my-bookings.html
    }

    // Customer view - Specific booking details
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-bookings/{id}")
    public String viewMyBookingDetails(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Booking booking = bookingService.getBookingById(id);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // Verify the booking belongs to the current user
            if (!userDetails.getUserId().equals(booking.getUser().getId())) {
                return "redirect:/bookings/my-bookings";
            }
            
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            
            model.addAttribute("booking", booking);
            model.addAttribute("nights", nights);
            return "booking/my-booking-details"; // templates/booking/my-booking-details.html
        } catch (RuntimeException e) {
            model.addAttribute("error", "Booking not found");
            return "redirect:/bookings/my-bookings";
        }
    }

   
    // Search bookings (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public String searchBookings(@RequestParam(required = false) String searchTerm,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Model model) {
        List<Booking> bookings;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // You would implement search in BookingService
            bookings = bookingService.getAllBookings(); // Placeholder
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("searchTerm", searchTerm);
        return "booking/search-bookings"; // templates/booking/search-bookings.html
    }

    // Cancel booking (Customer only for their own bookings)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.getBookingById(id);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // Verify the booking belongs to the current user
            if (!userDetails.getUserId().equals(booking.getUser().getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only cancel your own bookings");
                return "redirect:/bookings/my-bookings";
            }
            
            // Check if booking can be cancelled (e.g., not in the past)
            if (booking.getCheckInDate().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Cannot cancel past bookings");
                return "redirect:/bookings/my-bookings";
            }
            
          
            
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
            return "redirect:/bookings/my-bookings";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/bookings/my-bookings";
        }
    }
}