package com.example.hotelbooking.config;

import com.example.hotelbooking.model.Role;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Load admin user if not exists
            userRepository.findByEmail("admin@hotel.com").ifPresentOrElse(
                user -> {}, // already exists
                () -> {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@hotel.com"); // Add email
                    admin.setFullName("System Administrator"); // Add full name
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.ADMIN);
                    admin.setAccountBalance(10000.0);
                    userRepository.save(admin);
                }
            );

            // Load customer user if not exists
            userRepository.findByEmail("customer@hotel.com").ifPresentOrElse(
                user -> {}, // already exists
                () -> {
                    User customer = new User();
                    customer.setUsername("customer");
                    customer.setEmail("customer@hotel.com"); // Add email
                    customer.setFullName("Test Customer"); // Add full name
                    customer.setPassword(passwordEncoder.encode("cust123"));
                    customer.setRole(Role.CUSTOMER);
                    customer.setAccountBalance(5000.0);
                    userRepository.save(customer);
                }
            );
        };
    }
}