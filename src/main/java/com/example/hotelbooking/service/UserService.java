package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.UserRegistrationDTO;
import com.example.hotelbooking.exception.UserNotFoundException;
import com.example.hotelbooking.model.Role;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Original method - register user with specified role
    public User registerUser(UserRegistrationDTO registrationDTO, Role role) {
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(registrationDTO.getFullName());
        user.setEmail(registrationDTO.getEmail());
        user.setUsername(registrationDTO.getFullName());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(role);
        user.setAccountBalance(0.0); // Set default balance

        return userRepository.save(user);
    }

    // Overloaded method - register user as CUSTOMER by default (for REST API)
    public User registerUser(UserRegistrationDTO registrationDTO) {
        return registerUser(registrationDTO, Role.CUSTOMER);
    }

    // Find user by email - improved with exception handling
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    // Find user by email - returns null if not found (keep original behavior)
    public User findByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Validate user credentials
    public boolean validateCredentials(String email, String password) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return false;
            }
            return passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    // Check if user exists by email
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // Get user by ID
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}