package com.example.hotelbooking.service;

import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));
    }

    public Room updateRoom(Long id, Room updatedRoom) {
        Room existingRoom = getRoomById(id); // reuse method

        existingRoom.setRoomNumber(updatedRoom.getRoomNumber());
        existingRoom.setType(updatedRoom.getType());
        existingRoom.setCapacity(updatedRoom.getCapacity());
        existingRoom.setPricePerNight(updatedRoom.getPricePerNight());
        existingRoom.setDiscountPercentage(updatedRoom.getDiscountPercentage());
        existingRoom.setAvailable(updatedRoom.isAvailable());
        existingRoom.setDescription(updatedRoom.getDescription());

        return roomRepository.save(existingRoom);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room not found with ID: " + id);
        }
        roomRepository.deleteById(id);
    }
}
