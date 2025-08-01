package com.example.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;

public class UserRegistrationDTO {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name can only contain letters and spaces")
    private String fullName;

    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    // Optional: Phone number
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", 
             message = "Please enter a valid phone number")
    private String phoneNumber;

    // Custom validation to ensure passwords match
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) {
            return true; // Let @NotBlank handle null validation
        }
        return password.equals(confirmPassword);
    }

    // Constructors
    public UserRegistrationDTO() {}

    public UserRegistrationDTO(String fullName, String email, String password, String confirmPassword) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Utility method to get display name
    public String getDisplayName() {
        return fullName != null ? fullName.trim() : "";
    }

    @Override
    public String toString() {
        return "UserRegistrationDTO{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}'; // Don't include password in toString for security
    }
}