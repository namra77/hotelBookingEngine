package com.example.hotelbooking.config;

import com.example.hotelbooking.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.beans.Customizer;
import java.io.IOException;


//    @Autowired
//    private CustomUserDetailsService userDetailsService;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf().disable()
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/", "/auth/login", "/login", "/register/**",
//                    "/rooms/view", "/book-room",
//                    "/css/**", "/js/**", "/img/**", "/webjars/**",
//                    "/index.html"
//                ).permitAll()
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                .requestMatchers("/customer/**").hasRole("CUSTOMER")
//                .anyRequest().authenticated()
//            )
//            .formLogin(form -> form
//            	    .loginPage("/login")           
//            	    .loginProcessingUrl("/login")  
//            	    .successHandler(customSuccessHandler())
//            	    .failureUrl("/login?error")    
//            	    .permitAll()
//            	)
//            	.logout(logout -> logout
//            	    .logoutUrl("/logout")
//            	    .logoutSuccessUrl("/login?logout")  
//            	    .permitAll()
//            	);
//
//        return http.build();
//    }
//
//  
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
//        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//        return builder.build();
//    }
//}
//
//

//	
//	    @Autowired
//	    private CustomUserDetailsService userDetailsService;
//
//	    @Bean
//	    public PasswordEncoder passwordEncoder() {
//	        return new BCryptPasswordEncoder();
//	    }
//
//	    @Bean
//	    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//	        return http.getSharedObject(AuthenticationManager.class);
//	    }
//
//	    @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//	        http
//	            .csrf().disable() // Disable CSRF for APIs (enable if using sessions + Thymeleaf login)
//	            .authorizeHttpRequests(auth -> auth
//	                .requestMatchers("/auth/**").permitAll() // Public endpoints like register/login
//	                .requestMatchers("/register/admin/**").hasRole("ADMIN")
//	                .requestMatchers("/register/customer/**").hasRole("CUSTOMER")
//	                .anyRequest().authenticated()
//	            )
//	            .formLogin().disable() 
//	            .httpBasic(); 
//
//	        return http.build();
//	    }
//	}


   
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
          
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated())
          
            .httpBasic(withDefaults());

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
  public AuthenticationSuccessHandler customSuccessHandler() {
      return new AuthenticationSuccessHandler() {
          @Override
          public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authentication) throws IOException, ServletException {
              String role = authentication.getAuthorities().stream()
                  .map(auth -> auth.getAuthority())
                  .findFirst()
                  .orElse("");

              if (role.equals("ROLE_ADMIN")) {
                  response.sendRedirect("/admin/dashboard");
              } else if (role.equals("ROLE_CUSTOMER")) {
                  response.sendRedirect("/customer/dashboard");
              } else {
                  response.sendRedirect("/");
              }
          }
      };
    }
}
    
