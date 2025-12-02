package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private Booker booker;
    private Item item;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Booker {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String name;
    }
}