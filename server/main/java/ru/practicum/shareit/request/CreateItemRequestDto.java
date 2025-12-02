package ru.practicum.shareit.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateItemRequestDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}