package com.example.hotelbooking.exception;

public class UnsupportedPaymentMethodException extends BusinessException {
    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}