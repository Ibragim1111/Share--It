package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.exception.NoAccessException;
import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.exceptions.exception.ConflictException;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public BookingDto createBooking(BookingRequestDto bookingDto, Long userId) {
        User booker = userService.getUserById(userId);
        Item item = itemService.getItemEntityById(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }


        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new IllegalArgumentException("Invalid booking dates");
        }

        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NoAccessException("Only item owner can update booking status");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Booking status already decided");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only booker or owner can view booking");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state, int from, int size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
            case REJECTED:
                BookingStatus status = BookingStatus.valueOf(state.name());
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, status, pageable);
                break;
            default: // ALL
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state, int from, int size) {
        userService.getUserById(ownerId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
                break;
            case WAITING:
            case REJECTED:
                BookingStatus status = BookingStatus.valueOf(state.name());
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, status, pageable);
                break;
            default: // ALL
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
