package ru.practicum.shareit.booking.dto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NewBookingRequest(
        @NotNull(message = "Id вещи не должен быть пустым")
        long itemId,

        @NotNull(message = "Укажите дату и время начала бронирования")

        LocalDateTime start,

        @NotNull(message = "Укажите дату и время окончания бронирования")

        LocalDateTime end) {
}
