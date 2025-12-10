package ru.practicum.shareit.item.dto;



import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;


public class ItemMapper {
    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(lastBooking != null ?
                        ItemDto.BookingInfo.builder()
                                .id(lastBooking.getId())
                                .bookerId(lastBooking.getBooker().getId())
                                .start(lastBooking.getStart())
                                .end(lastBooking.getEnd())
                                .build() : null)
                .nextBooking(nextBooking != null ?
                        ItemDto.BookingInfo.builder()
                                .id(nextBooking.getId())
                                .bookerId(nextBooking.getBooker().getId())
                                .start(nextBooking.getStart())
                                .end(nextBooking.getEnd())
                                .build() : null)
                .comments(comments)
                .build();
    }

    public static ItemDto toItemDto(Item item, List<CommentDto> comments) {
        return toItemDto(item, null, null, comments);
    }

    // Остальные методы остаются без изменений
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public static Item toItem(NewItemDto itemDto, ru.practicum.shareit.user.User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .requestId(itemDto.getRequestId())
                .build();
    }

    public static Item updateItemFields(Item existingItem, UpdateItemDto request) {
        return existingItem.toBuilder()
                .name(request.getName() != null ? request.getName() : existingItem.getName())
                .description(request.getDescription() != null ? request.getDescription() : existingItem.getDescription())
                .available(request.getAvailable() != null ? request.getAvailable() : existingItem.getAvailable())
                .requestId(request.getRequestId() != null ? request.getRequestId() : existingItem.getRequestId())
                .build();
    }
}
