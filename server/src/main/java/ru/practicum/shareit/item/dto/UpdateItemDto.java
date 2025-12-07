package ru.practicum.shareit.item.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor
public class UpdateItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId; // будет использоваться позже для запросов
}
