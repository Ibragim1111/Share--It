package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;

import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comments.*;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Item createItem(Item item, Long ownerId) {
        var owner = userService.getUserById(ownerId);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Override
    public Item getItemEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));


        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            Optional<Booking> lastBookingOpt = bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            id, LocalDateTime.now(), BookingStatus.APPROVED);
            Optional<Booking> nextBookingOpt = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            id, LocalDateTime.now(), BookingStatus.APPROVED);
            lastBooking = lastBookingOpt.orElse(null);
            nextBooking = nextBookingOpt.orElse(null);
        }


        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(id)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> getItemsByOwnerId(Long ownerId) {
        userService.getUserById(ownerId);

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());


        Map<Long, Booking> lastBookings = bookingRepository
                .findLastBookingsForItems(itemIds, LocalDateTime.now(), BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));

        Map<Long, Booking> nextBookings = bookingRepository
                .findNextBookingsForItems(itemIds, LocalDateTime.now(), BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));


        Map<Long, List<CommentDto>> commentsByItem = commentRepository
                .findByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));


        return items.stream()
                .map(item -> {
                    Booking lastBooking = lastBookings.get(item.getId());
                    Booking nextBooking = nextBookings.get(item.getId());
                    List<CommentDto> comments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());

                    return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Item updateItem(Long itemId, UpdateItemDto itemUpdate, Long ownerId) {
        Item existingItem = getItemById(itemId, ownerId).getId() != null ?
                itemRepository.findById(itemId).get() : null;

        if (existingItem == null) {
            throw new NotFoundException("Item not found with id: " + itemId);
        }


        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("User is not the owner of the item");
        }

        Item updatedItem = ItemMapper.updateItemFields(existingItem, itemUpdate);
        return itemRepository.save(updatedItem);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableItems(text);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CreateCommentDto commentDto) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));
        var user = userService.getUserById(userId);


        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (!hasBooked) {
            throw new IllegalArgumentException("User can only comment on items they have booked in the past");
        }


        Comment comment = CommentMapper.toComment(commentDto, item, user);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }


}