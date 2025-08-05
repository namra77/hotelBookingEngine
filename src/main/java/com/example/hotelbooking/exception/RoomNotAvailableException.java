package com.example.hotelbooking.exception;

public class RoomNotAvailableException extends BusinessException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}