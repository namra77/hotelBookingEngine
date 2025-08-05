package com.example.hotelbooking.exception;

public class BookingNotFoundException extends BusinessException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}