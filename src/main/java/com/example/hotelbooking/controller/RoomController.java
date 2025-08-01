package com.example.hotelbooking.controller;

import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.service.RoomService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    // -------------------------------
    // ✅ REST API Endpoints (JSON)
    // -------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/rooms")
    @ResponseBody
    public Room addRoom(@Valid @RequestBody Room room) {
        return roomService.addRoom(room);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/api/rooms")
    @ResponseBody
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/api/rooms/{id}")
    @ResponseBody
    public Room getRoomById(@PathVariable("id") Long id) {
        return roomService.getRoomById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/rooms/{id}")
    @ResponseBody
    public Room updateRoom(@PathVariable Long id, @RequestBody Room room) {
        return roomService.updateRoom(id, room);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/rooms/{id}")
    @ResponseBody
    public void deleteRoomViaApi(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }

    // -----------------------------------
    // ✅ Frontend Thymeleaf View Endpoints
    // -----------------------------------

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/rooms/view")
    public String viewAllRooms(Model model) {
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "customer/room-list"; // templates/customer/rooms.html
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/add")
    public String showAddRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/room-add"; // templates/admin/room-add.html
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/add")
    public String processAddRoom(@ModelAttribute("room") Room room) {
        roomService.addRoom(room);
        return "redirect:/rooms/view/all-rooms";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/edit/{id}")
    public String showEditRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "admin/room-edit"; // templates/admin/room-edit.html
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/edit/{id}")
    public String updateRoomFromForm(@PathVariable Long id, @ModelAttribute("room") Room room) {
        roomService.updateRoom(id, room);
        return "redirect:/rooms/view/all-rooms";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return "redirect:/rooms/view/all-rooms";
    }
    
    @GetMapping("/rooms")
    public String showAllRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "room/room-list"; 
    }
}
