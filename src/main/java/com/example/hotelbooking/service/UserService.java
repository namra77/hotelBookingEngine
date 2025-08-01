package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.UserRegistrationDTO;
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

    // ✅ This method is used by the controller for both CUSTOMER and ADMIN registration
    public User registerUser(UserRegistrationDTO registrationDTO, Role role) {
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(registrationDTO.getFullName());
        user.setEmail(registrationDTO.getEmail());
        user.setUsername(registrationDTO.getFullName()); // assuming your DTO includes this
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(role);

        return userRepository.save(user);
    }

    // Optional method to find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
