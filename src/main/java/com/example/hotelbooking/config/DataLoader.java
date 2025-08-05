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
            System.out.println("=== DataLoader: Starting to load initial data ===");
            
            // Load admin user if not exists
            userRepository.findByEmail("admin@hotel.com").ifPresentOrElse(
                user -> {
                    System.out.println("Admin user already exists: " + user.getEmail());
                }, 
                () -> {
                    System.out.println("Creating admin user...");
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@hotel.com");
                    admin.setFullName("System Administrator");
                    
                    String rawPassword = "admin123";
                    String encodedPassword = passwordEncoder.encode(rawPassword);
                    admin.setPassword(encodedPassword);
                    admin.setRole(Role.ADMIN);
                    admin.setAccountBalance(10000.0);
                    
                    User savedAdmin = userRepository.save(admin);
                    System.out.println("Admin user created successfully with ID: " + savedAdmin.getId());
                    System.out.println("Admin credentials: admin@hotel.com / admin123");
                    System.out.println("Encoded password: " + encodedPassword);
                }
            );

            // Load customer user if not exists
            userRepository.findByEmail("customer@hotel.com").ifPresentOrElse(
                user -> {
                    System.out.println("Customer user already exists: " + user.getEmail());
                }, 
                () -> {
                    System.out.println("Creating customer user...");
                    User customer = new User();
                    customer.setUsername("customer");
                    customer.setEmail("customer@hotel.com");
                    customer.setFullName("Test Customer");
                    
                    String rawPassword = "cust123";
                    String encodedPassword = passwordEncoder.encode(rawPassword);
                    customer.setPassword(encodedPassword);
                    customer.setRole(Role.CUSTOMER);
                    customer.setAccountBalance(5000.0);
                    
                    User savedCustomer = userRepository.save(customer);
                    System.out.println("Customer user created successfully with ID: " + savedCustomer.getId());
                    System.out.println("Customer credentials: customer@hotel.com / cust123");
                    System.out.println("Encoded password: " + encodedPassword);
                }
            );
            
            System.out.println("=== DataLoader: Data loading completed ===");
            
            // Print all users for debugging
            System.out.println("=== All users in database ===");
            userRepository.findAll().forEach(user -> {
                System.out.println("User: " + user.getEmail() + " | Role: " + user.getRole() + " | ID: " + user.getId());
            });
        };
    }
}