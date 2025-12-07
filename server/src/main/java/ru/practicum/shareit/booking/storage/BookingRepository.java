package ru.practicum.shareit.booking.storage;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);

    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime date, BookingStatus status);


    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime date, BookingStatus status);


    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime end, BookingStatus status);


    @Query("""
        SELECT b FROM Booking b WHERE b.item.id IN :itemIds
        AND b.start < :now AND b.status = :status
        AND b.id IN (SELECT MAX(b2.id) FROM Booking b2 WHERE b2.item.id = b.item.id
        AND b2.start < :now AND b2.status = :status GROUP BY b2.item.id)
        """)
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") BookingStatus status);


    @Query("""
        SELECT b FROM Booking b WHERE b.item.id IN :itemIds
        AND b.start > :now AND b.status = :status
        AND b.id IN (SELECT MIN(b2.id) FROM Booking b2 WHERE b2.item.id = b.item.id
        AND b2.start > :now AND b.status = :status GROUP BY b2.item.id)""")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") BookingStatus status);


}
