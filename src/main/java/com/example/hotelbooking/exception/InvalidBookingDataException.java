package com.example.hotelbooking.exception;

public class InvalidBookingDataException extends BusinessException {
    public InvalidBookingDataException(String message) {
        super(message);
    }
}