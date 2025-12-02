package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;


import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;


    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsByOwnerId(ownerId);
    }


    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody CreateCommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }


    @PostMapping
    public ItemDto createItem(@Valid @RequestBody NewItemDto itemDto,
                              @Valid @RequestHeader(value = "X-Sharer-User-Id") Long ownerId) {
        var item = ItemMapper.toItem(itemDto, null);
        var createdItem = itemService.createItem(item, ownerId);
        return ItemMapper.toItemDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody UpdateItemDto itemDto,
                              @RequestHeader(value = "X-Sharer-User-Id") Long ownerId) {
        var updatedItem = itemService.updateItem(itemId, itemDto, ownerId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}