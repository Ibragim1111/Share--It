package ru.practicum.shareit.item;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;

import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createItem_whenValid_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.createItem(item, owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        verify(itemRepository).save(item);
    }

    @Test
    void getItemEntityById_whenExists_thenSuccess() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Item result = itemService.getItemEntityById(1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
    }

    @Test
    void getItemEntityById_whenNotExists_thenThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.getItemEntityById(999L));
    }

    @Test
    void getItemById_whenOwner_thenWithBookings() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(1L, owner.getId());

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
    }

    @Test
    void getItemById_whenNotOwner_thenWithoutBookings() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(1L, 999L);

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNotNull(result.getComments());
    }

    @Test
    void updateItem_whenValid_thenSuccess() {
        Item existingItem = item.toBuilder().build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        Item result = itemService.updateItem(1L, updateDto, owner.getId());

        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_whenNotOwner_thenThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Name")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, updateDto, 999L));
    }

    @Test
    void searchItems_whenValidText_thenSuccess() {
        when(itemRepository.searchAvailableItems(anyString())).thenReturn(List.of(item));

        List<Item> result = itemService.searchItems("item");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchItems_whenBlankText_thenEmptyList() {
        List<Item> result = itemService.searchItems(" ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_whenValid_thenSuccess() {
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                anyLong(), anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, booker.getId(), commentDto);

        assertNotNull(result);
        assertEquals(comment.getText(), result.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_whenNotBooked_thenThrowException() {
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Great item!")
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getUserById(anyLong())).thenReturn(booker);
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                anyLong(), anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(1L, booker.getId(), commentDto));
    }
}
