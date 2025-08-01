
package com.example.hotelbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class HotelBookingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingEngineApplication.class, args);
        System.out.println("🏨 Hotel Booking Engine Application Started Successfully!");
        System.out.println("📋 Default users created:");
        System.out.println("   Admin: admin@hotel.com / admin123");
        System.out.println("   Customer: customer@hotel.com / cust123");
        System.out.println("🌐 Access the application at: http://localhost:8080");
    }
}