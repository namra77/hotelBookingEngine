package com.example.hotelbooking.config;

import com.example.hotelbooking.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management for stateless REST API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/check-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/available").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/{id}/availability").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/available-rooms").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/check-availability").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Customer booking endpoints
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bookings/validate").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/my").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/my/{id}").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/user/{userId}").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/{id}/user/{userId}").hasRole("CUSTOMER")
                
                // Admin booking management
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/{id}/admin").hasRole("ADMIN")
                
                // Booking details - accessible by both admin and customer
                .requestMatchers(HttpMethod.GET, "/api/bookings/{id}").authenticated()
                
                // User profile endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/profile").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure HTTP Basic authentication
            .httpBasic(Customizer.withDefaults())
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from frontend domains
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080" // Local testing
            
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials (for authentication)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}