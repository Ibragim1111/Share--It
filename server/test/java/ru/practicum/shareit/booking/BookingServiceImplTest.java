package ru.practicum.shareit.booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.exception.ConflictException;
import ru.practicum.shareit.exceptions.exception.NoAccessException;
import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .id(1L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        owner = User.builder()
                .id(2L)
                .name("Owner")
                .email("owner@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_whenValid_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(itemService.getItemEntityById(anyLong())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.createBooking(bookingRequestDto, booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotAvailable_thenThrowException() {
        item.setAvailable(false);
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(itemService.getItemEntityById(anyLong())).thenReturn(item);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(bookingRequestDto, booker.getId()));
    }

    @Test
    void createBooking_whenEndBeforeStart_thenThrowException() {
        bookingRequestDto.setEnd(LocalDateTime.now().minusDays(1));
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(itemService.getItemEntityById(anyLong())).thenReturn(item);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(bookingRequestDto, booker.getId()));
    }

    @Test
    void updateBookingStatus_whenApproved_thenSuccess() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.updateBookingStatus(1L, true, owner.getId());

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingStatus_whenNotOwner_thenThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NoAccessException.class,
                () -> bookingService.updateBookingStatus(1L, true, 999L));
    }

    @Test
    void updateBookingStatus_whenStatusAlreadyDecided_thenThrowException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class,
                () -> bookingService.updateBookingStatus(1L, true, owner.getId()));
    }

    @Test
    void getBookingById_whenBooker_thenSuccess() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBookingById_whenNotBookerOrOwner_thenThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(1L, 999L));
    }

    @Test
    void getUserBookings_whenAllState_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(
                booker.getId(), BookingState.ALL, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenCurrentState_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getOwnerBookings(
                owner.getId(), BookingState.CURRENT, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
