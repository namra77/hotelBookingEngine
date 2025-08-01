package com.example.hotelbooking.security;

import com.example.hotelbooking.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    
    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Using email as username
    }

    // CUSTOM METHODS for accessing user data
    public Long getUserId() {
        return user.getId();
    }
    
    public User getUser() {
        return user;
    }
    
    public String getFullName() {
        return user.getFullName();
    }
    
    public Double getAccountBalance() {
        return user.getAccountBalance();
    }

    // ACCOUNT STATUS METHODS - Default to true, customize as needed
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // You could add an 'enabled' field to User entity if needed
    }
}