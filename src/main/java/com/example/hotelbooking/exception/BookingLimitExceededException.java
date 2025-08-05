package com.example.hotelbooking.exception;

public class BookingLimitExceededException extends BusinessException {
    public BookingLimitExceededException(String message) {
        super(message);
    }
}