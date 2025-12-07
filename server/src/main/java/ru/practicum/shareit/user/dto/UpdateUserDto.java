package ru.practicum.shareit.user.dto;


import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor
public class UpdateUserDto {
    private Long id;

    private String name;

    @Email(message = "Электронная почта должна содержать символ @")
    private String email;

}
