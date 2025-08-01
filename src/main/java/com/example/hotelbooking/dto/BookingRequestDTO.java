package com.example.hotelbooking.dto;

import com.example.hotelbooking.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class BookingRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkIn;

    @NotNull(message = "Check-out date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOut;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String notes; // Optional field for special requests

    // Custom validation to ensure check-in is before check-out
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkIn == null || checkOut == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkIn.isBefore(checkOut);
    }

    // Additional validation to ensure dates are not in the past
    @AssertTrue(message = "Check-in date cannot be in the past")
    public boolean isCheckInDateValid() {
        if (checkIn == null) {
            return true; // Let @NotNull handle null validation
        }
        return !checkIn.isBefore(LocalDate.now());
    }

    // Constructors
    public BookingRequestDTO() {}

    public BookingRequestDTO(Long userId, Long roomId, LocalDate checkIn, LocalDate checkOut, PaymentMethod paymentMethod) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
    }

    // --- Getters and Setters ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility method to calculate number of nights
    public long getNumberOfNights() {
        if (checkIn != null && checkOut != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "BookingRequestDTO{" +
                "userId=" + userId +
                ", roomId=" + roomId +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", paymentMethod=" + paymentMethod +
                ", notes='" + notes + '\'' +
                '}';
    }
}