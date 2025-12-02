package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import ru.practicum.shareit.item.comments.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


public interface ItemService {
    Item createItem(Item item, Long ownerId);

    Item getItemEntityById(Long id);

    ItemDto getItemById(Long id, Long userId);

    List<ItemDto> getItemsByOwnerId(Long ownerId);

    Item updateItem(Long itemId, UpdateItemDto item, Long ownerId);

    List<Item> searchItems(String text);

    CommentDto addComment(Long itemId, Long userId, CreateCommentDto commentDto);
}
