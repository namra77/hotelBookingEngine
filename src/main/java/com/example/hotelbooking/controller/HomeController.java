package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.UserRegistrationDTO;
import com.example.hotelbooking.model.Role;
import com.example.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Welcome to Hotel Booking Engine");
        return "index"; // templates/index.html
    }

    
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid UserRegistrationDTO userDto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto, Role.CUSTOMER);
        } catch (RuntimeException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }

        return "redirect:/login?success";
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(
                auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(
                auth -> auth.getAuthority().equals("ROLE_CUSTOMER"))) {
            return "redirect:/customer/dashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/dashboard"; // templates/customer/dashboard.html
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard"; // templates/admin/dashboard.html
    }
}
