//package com.example.hotelbooking.service;
//
//import com.example.hotelbooking.model.*;
//import com.example.hotelbooking.repository.BookingRepository;
//import com.example.hotelbooking.repository.RoomRepository;
//import com.example.hotelbooking.repository.UserRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class BookingServiceTest {
//
//    @InjectMocks
//    private BookingService bookingService;
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    private AutoCloseable closeable;
//
//    private User mockUser;
//    private Room mockRoom;
//    private LocalDate checkIn;
//    private LocalDate checkOut;
//
//    @BeforeEach
//    void setUp() {
//        closeable = MockitoAnnotations.openMocks(this);
//
//        mockUser = new User();
//        mockUser.setId(1L);
//        mockUser.setAccountBalance(1000.0);
//        mockUser.setEmail("test@example.com");
//        mockUser.setFullName("Test User");
//
//        mockRoom = new Room();
//        mockRoom.setId(1L);
//        mockRoom.setAvailable(true);
//        mockRoom.setPricePerNight(200);
//        mockRoom.setDiscountPercentage(10);
//        mockRoom.setRoomNumber("A101");
//        mockRoom.setType("Deluxe");
//
//        checkIn = LocalDate.now().plusDays(1);
//        checkOut = LocalDate.now().plusDays(3);
//    }
//
//    @AfterEach
//    void tearDown() throws Exception {
//        closeable.close();
//    }
//
//    @Test
//    void testCreateBooking_success_withAccountBalance() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));
//        when(bookingRepository.existsByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(anyLong(), any(), any()))
//                .thenReturn(false);
//        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Booking booking = bookingService.createBooking(
//                1L, 1L, checkIn, checkOut, PaymentMethod.ACCOUNT_BALANCE
//        );
//
//        assertNotNull(booking);
//        assertEquals(PaymentMethod.ACCOUNT_BALANCE, booking.getPaymentMethod());
//        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
//        verify(userRepository).save(mockUser);  // account balance deducted
//        verify(bookingRepository).save(any(Booking.class));
//    }
//}
