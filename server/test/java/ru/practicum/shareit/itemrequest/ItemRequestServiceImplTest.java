package ru.practicum.shareit.itemrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.CreateItemRequestDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requester;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .id(1L)
                .name("Requester")
                .email("requester@email.com")
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(requester)
                .requestId(request.getId())
                .build();
    }

    @Test
    void createRequest_whenValid_thenSuccess() {
        CreateItemRequestDto requestDto = CreateItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(userService.getUserById(anyLong())).thenReturn(requester);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto result = requestService.createRequest(requestDto, requester.getId());

        assertNotNull(result);
        assertEquals(request.getDescription(), result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequests_whenValid_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(requester);
        when(requestRepository.findByRequesterIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getUserRequests(requester.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void getOtherUsersRequests_whenValid_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(requester);
        when(requestRepository.findByRequesterIdNotOrderByCreatedDesc(
                anyLong(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getOtherUsersRequests(requester.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRequestById_whenExists_thenSuccess() {
        when(userService.getUserById(anyLong())).thenReturn(requester);
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = requestService.getRequestById(1L, requester.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getRequestById_whenNotExists_thenThrowException() {
        when(userService.getUserById(anyLong())).thenReturn(requester);
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, requester.getId()));
    }
}
