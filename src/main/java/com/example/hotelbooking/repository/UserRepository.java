package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.Role;
import com.example.hotelbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email or username
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Find users by role
    List<User> findByRole(Role role);
    
    // Find users by role with pagination
    Page<User> findByRole(Role role, Pageable pageable);
    
    // Search users by name or email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find users with low account balance
    @Query("SELECT u FROM User u WHERE u.accountBalance < :threshold AND u.role = 'CUSTOMER'")
    List<User> findUsersWithLowBalance(@Param("threshold") Double threshold);
    
    // Find users by account balance range
    List<User> findByAccountBalanceBetween(Double minBalance, Double maxBalance);
    
    // Count users by role
    long countByRole(Role role);
    
    // Find top customers by account balance
    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' ORDER BY u.accountBalance DESC")
    List<User> findTopCustomersByBalance(Pageable pageable);
}