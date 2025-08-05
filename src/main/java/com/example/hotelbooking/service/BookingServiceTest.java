//package com.example.hotelbooking.service;
//
//import com.example.hotelbooking.exception.*;
//import com.example.hotelbooking.model.*;
//import com.example.hotelbooking.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ActiveProfiles;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.LockModeType;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@ActiveProfiles("test")
//class BookingServiceImplTest {
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private EntityManager entityManager;
//
//    @InjectMocks
//    private BookingServiceImpl bookingService;
//
//    private User testCustomer;
//    private User testAdmin;
//    private Room testRoom;
//    private LocalDate checkIn;
//    private LocalDate checkOut;
//
//    @BeforeEach
//    void setUp() {
//        // Create test customer
//        testCustomer = new User();
//        testCustomer.setId(1L);
//        testCustomer.setUsername("testcustomer");
//        testCustomer.setEmail("test@customer.com");
//        testCustomer.setFullName("Test Customer");
//        testCustomer.setRole(Role.CUSTOMER);
//        testCustomer.setAccountBalance(1000.0);
//
//        // Create test admin
//        testAdmin = new User();
//        testAdmin.setId(2L);
//        testAdmin.setUsername("testadmin");
//        testAdmin.setEmail("test@admin.com");
//        testAdmin.setFullName("Test Admin");
//        testAdmin.setRole(Role.ADMIN);
//        testAdmin.setAccountBalance(5000.0);
//
//        // Create test room
//        testRoom = new Room();
//        testRoom.setId(1L);
//        testRoom.setRoomNumber("101");
//        testRoom.setType("Single");
//        testRoom.setCapacity(2);
//        testRoom.setPricePerNight(100.0);
//        testRoom.setDiscountPercentage(0.0);
//        testRoom.setAvailable(true);
//        testRoom.setDescription("Test room");
//
//        // Set test dates
//        checkIn = LocalDate.now().plusDays(1);
//        checkOut = LocalDate.now().plusDays(3);
//    }
//
//    @Test
//    void createBooking_Success() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
//        when(bookingRepository.findConflictingBookings(1L, checkIn, checkOut, BookingStatus.CONFIRMED))
//            .thenReturn(new ArrayList<>());
//        
//        Booking savedBooking = createTestBooking();
//        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
//
//        // Act
//        Booking result = bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(testCustomer, result.getUser());
//        assertEquals(testRoom, result.getRoom());
//        assertEquals(checkIn, result.getCheckInDate());
//        assertEquals(checkOut, result.getCheckOutDate());
//        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
//        
//        // Verify user's account balance was deducted
//        verify(userRepository).save(testCustomer);
//        assertEquals(800.0, testCustomer.getAccountBalance(), 0.01); // 1000 - 200 = 800
//    }
//
//    @Test
//    void createBooking_UserNotFound_ThrowsException() {
//        // Arrange
//        when(userRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> 
//            bookingService.createBooking(999L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_RoomNotFound_ThrowsException() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 999L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);
//
//        // Act & Assert
//        assertThrows(RoomNotFoundException.class, () -> 
//            bookingService.createBooking(1L, 999L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_InvalidDates_ThrowsException() {
//        // Act & Assert - Check-in date in the past
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.createBooking(1L, 1L, LocalDate.now().minusDays(1), checkOut, PaymentMethod.ACCOUNT_BALANCE));
//
//        // Act & Assert - Check-out before check-in
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkOut, checkIn, PaymentMethod.ACCOUNT_BALANCE));
//
//        // Act & Assert - Same day check-in and check-out
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkIn, checkIn, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_RoomNotAvailable_ThrowsException() {
//        // Arrange
//        testRoom.setAvailable(false);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//
//        // Act & Assert
//        assertThrows(RoomNotAvailableException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_AdminUser_ThrowsException() {
//        // Arrange
//        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//
//        // Act & Assert
//        assertThrows(UnauthorizedBookingException.class, () -> 
//            bookingService.createBooking(2L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_BookingConflict_ThrowsException() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
//        
//        // Create conflicting booking
//        List<Booking> conflictingBookings = List.of(createTestBooking());
//        when(bookingRepository.findConflictingBookings(1L, checkIn, checkOut, BookingStatus.CONFIRMED))
//            .thenReturn(conflictingBookings);
//
//        // Act & Assert
//        assertThrows(BookingConflictException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_InsufficientFunds_ThrowsException() {
//        // Arrange
//        testCustomer.setAccountBalance(50.0); // Less than required 200.0
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
//        when(bookingRepository.findConflictingBookings(1L, checkIn, checkOut, BookingStatus.CONFIRMED))
//            .thenReturn(new ArrayList<>());
//
//        // Act & Assert
//        assertThrows(InsufficientFundsException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_BookingLimitExceeded_ThrowsException() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(3L); // Max limit reached
//
//        // Act & Assert
//        assertThrows(BookingLimitExceededException.class, () -> 
//            bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE));
//    }
//
//    @Test
//    void createBooking_WithDiscount_CalculatesCorrectPrice() {
//        // Arrange
//        testRoom.setDiscountPercentage(10.0); // 10% discount
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
//        when(bookingRepository.findConflictingBookings(1L, checkIn, checkOut, BookingStatus.CONFIRMED))
//            .thenReturn(new ArrayList<>());
//        
//        Booking savedBooking = createTestBooking();
//        savedBooking.setTotalPrice(180.0); // 200 - 10% = 180
//        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
//
//        // Act
//        Booking result = bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE);
//
//        // Assert
//        assertEquals(180.0, result.getTotalPrice(), 0.01);
//        assertEquals(820.0, testCustomer.getAccountBalance(), 0.01); // 1000 - 180 = 820
//    }
//
//    @Test
//    void createBooking_CreditCardPayment_Success() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
//        when(entityManager.find(Room.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(testRoom);
//        when(bookingRepository.countByUserIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
//        when(bookingRepository.findConflictingBookings(1L, checkIn, checkOut, BookingStatus.CONFIRMED))
//            .thenReturn(new ArrayList<>());
//        
//        Booking savedBooking = createTestBooking();
//        savedBooking.setPaymentMethod(PaymentMethod.CREDIT_CARD);
//        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
//
//        // Act
//        Booking result = bookingService.createBooking(1L, 1L, checkIn, checkOut, PaymentMethod.CREDIT_CARD);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
//        // Account balance should remain unchanged for credit card payment
//        assertEquals(1000.0, testCustomer.getAccountBalance(), 0.01);
//    }
//
//    @Test
//    void cancelBooking_Success() {
//        // Arrange
//        Booking booking = createTestBooking();
//        booking.setId(1L);
//        booking.setStatus(BookingStatus.CONFIRMED);
//        booking.setCheckInDate(LocalDate.now().plusDays(2)); // Future date
//        booking.setPaymentMethod(PaymentMethod.ACCOUNT_BALANCE);
//        
//        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
//        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
//        when(userRepository.save(any(User.class))).thenReturn(testCustomer);
//
//        // Act
//        bookingService.cancelBooking(1L, 1L);
//
//        // Assert
//        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
//        verify(bookingRepository).save(booking);
//        // Verify refund was processed (90% of 200 = 180, so balance becomes 1000 + 180 = 1180)
//        assertEquals(1180.0, testCustomer.getAccountBalance(), 0.01);
//    }
//
//    @Test
//    void cancelBooking_UnauthorizedUser_ThrowsException() {
//        // Arrange
//        Booking booking = createTestBooking();
//        booking.setId(1L);
//        User otherUser = new User();
//        otherUser.setId(999L);
//        booking.setUser(otherUser);
//        
//        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
//
//        // Act & Assert
//        assertThrows(UnauthorizedBookingException.class, () -> 
//            bookingService.cancelBooking(1L, 1L));
//    }
//
//    @Test
//    void cancelBooking_TooCloseToCheckIn_ThrowsException() {
//        // Arrange
//        Booking booking = createTestBooking();
//        booking.setId(1L);
//        booking.setCheckInDate(LocalDate.now()); // Today (less than 24 hours)
//        
//        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
//
//        // Act & Assert
//        assertThrows(BookingCancellationException.class, () -> 
//            bookingService.cancelBooking(1L, 1L));
//    }
//
//    @Test
//    void cancelBooking_AlreadyCancelled_ThrowsException() {
//        // Arrange
//        Booking booking = createTestBooking();
//        booking.setId(1L);
//        booking.setStatus(BookingStatus.CANCELLED);
//        
//        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
//
//        // Act & Assert
//        assertThrows(BookingCancellationException.class, () -> 
//            bookingService.cancelBooking(1L, 1L));
//    }
//
//    @Test
//    void getAvailableRooms_Success() {
//        // Arrange
//        List<Room> availableRooms = List.of(testRoom);
//        when(roomRepository.findAvailableRooms(checkIn, checkOut)).thenReturn(availableRooms);
//
//        // Act
//        List<Room> result = bookingService.getAvailableRooms(checkIn, checkOut);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(testRoom, result.get(0));
//    }
//
//    @Test
//    void getAvailableRooms_InvalidDates_ThrowsException() {
//        // Act & Assert
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.getAvailableRooms(null, checkOut));
//        
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.getAvailableRooms(checkIn, null));
//        
//        assertThrows(InvalidBookingDataException.class, () -> 
//            bookingService.getAvailableRooms(LocalDate.now().minusDays(1), checkOut));
//    }
//
//    @Test
//    void getBookingsByUserId_UserNotFound_ThrowsException() {
//        // Arrange
//        when(userRepository.existsById(999L)).thenReturn(false);
//
//        // Act & Assert
//        assertThrows(UserNotFoundException.class, () -> 
//            bookingService.getBookingsByUserId(999L));
//    }
//
//    @Test
//    void getBookingById_NotFound_ThrowsException() {
//        // Arrange
//        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(BookingNotFoundException.class, () -> 
//            bookingService.getBookingById(999L));
//    }
//
//    // Helper method to create test booking
//    private Booking createTestBooking() {
//        Booking booking = new Booking();
//        booking.setUser(testCustomer);
//        booking.setRoom(testRoom);
//        booking.setCheckInDate(checkIn);
//        booking.setCheckOutDate(checkOut);
//        booking.setTotalPrice(200.0); // 2 nights * 100.0 per night
//        booking.setPaymentMethod(PaymentMethod.ACCOUNT_BALANCE);
//        booking.setStatus(BookingStatus.CONFIRMED);
//        return booking;
//    }
//}
//
//// ===== Integration Test Example =====
//
//package com.example.hotelbooking.service;
//
//import com.example.hotelbooking.model.*;
//import com.example.hotelbooking.repository.*;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class BookingServiceIntegrationTest {
//
//    @Autowired
//    private BookingService bookingService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoomRepository roomRepository;
//
//    @Test
//    void concurrentBookingAttempts_OnlyOneSucceeds() throws ExecutionException, InterruptedException {
//        // Arrange - Create test data
//        User user1 = createTestUser("user1@test.com");
//        User user2 = createTest