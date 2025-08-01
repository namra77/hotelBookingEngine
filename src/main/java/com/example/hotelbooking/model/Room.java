package com.example.hotelbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(unique = true)
    private String roomNumber; // e.g., "101", "B2"

    @NotBlank(message = "Room type is required")
    private String type; // e.g., "Single", "Double", "Suite"

    @Min(1)
    private int capacity; // Number of guests the room can hold

    @DecimalMin(value = "0.0", inclusive = false)
    private double pricePerNight; // Cost per night

    @Min(0)
    @Max(100)
    private double discountPercentage; // e.g., 10 for 10%

    private boolean available = true; // Room availability

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description; // Optional details
    
    private String imageUrl; // Optional: store image path or filename

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public double getPricePerNight() {
		return pricePerNight;
	}

	public void setPricePerNight(double pricePerNight) {
		this.pricePerNight = pricePerNight;
	}

	public double getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(double discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
    
    
}
