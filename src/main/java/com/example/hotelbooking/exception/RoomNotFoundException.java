package com.example.hotelbooking.exception;

public class RoomNotFoundException extends BusinessException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
