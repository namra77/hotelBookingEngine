package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.UserRegistrationDTO;
import com.example.hotelbooking.model.Role;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

   
    // ========= REGISTER CUSTOMER FORM ==========
    @GetMapping("/register/customer")
    public String registerCustomerForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "auth/register-customer";
    }

    @PostMapping("/register/customer")
    public String registerCustomerSubmit(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register-customer";
        }

        userService.registerUser(userDTO, Role.CUSTOMER);
        return "redirect:/auth/login?registered";
    }

    // ========= REGISTER ADMIN FORM ==========
    @GetMapping("/register/manager")
    public String registerManagerForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register-manager";
    }

    @PostMapping("/register/manager")
    public String registerManagerSubmit(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                                        BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register-manager";
        }

        userService.registerUser(userDTO, Role.ADMIN);
        return "redirect:/auth/login?registered";
    }

    // ========= OPTIONAL: Register via JSON ==========
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<User> registerJson(@Valid @RequestBody UserRegistrationDTO userDTO) {
        User savedUser = userService.registerUser(userDTO, Role.CUSTOMER);
        return ResponseEntity.ok(savedUser);
    }

    // ========= UTILITY ROUTES ==========
    @GetMapping("/me")
    @ResponseBody
    public String getLoggedInUser(Authentication authentication) {
        return "You are logged in as: " + authentication.getName();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    @ResponseBody
    public String adminTest() {
        return "Only admins can see this.";
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer-only")
    @ResponseBody
    public String customerTest() {
        return "Only customers can see this.";
    }
}
