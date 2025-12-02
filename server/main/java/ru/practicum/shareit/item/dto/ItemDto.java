package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.comments.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;

    // Информация о бронированиях
    private BookingInfo lastBooking;
    private BookingInfo nextBooking;

    // Список комментариев
    private List<CommentDto> comments;

    @Data
    @Builder
    @AllArgsConstructor
    public static class BookingInfo {
        private Long id;
        private Long bookerId;
        private LocalDateTime start;
        private LocalDateTime end;
    }
}