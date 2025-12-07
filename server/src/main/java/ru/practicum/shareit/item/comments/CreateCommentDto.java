package ru.practicum.shareit.item.comments;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor // ← ДОБАВИТЬ ЭТО!
public class CreateCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
