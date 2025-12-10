package ru.practicum.shareit.item.comments;



import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CreateCommentDto commentDto, Item item,
                                    User author) {
        return Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(java.time.LocalDateTime.now())
                .build();
    }
}
