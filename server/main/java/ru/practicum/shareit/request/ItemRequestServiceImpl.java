package ru.practicum.shareit.request;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.CreateItemRequestDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(CreateItemRequestDto requestDto, Long userId) {
        User requester = userService.getUserById(userId);

        ItemRequest request = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = requestRepository.save(request);
        return toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(Long userId, int from, int size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findByRequesterIdNotOrderByCreatedDesc(userId, pageable);
        return requests.stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userService.getUserById(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found with id: " + requestId));
        return toItemRequestDto(request);
    }

    private ItemRequestDto toItemRequestDto(ItemRequest request) {
        List<Item> items = itemRepository.findByRequestId(request.getId());

        List<ItemRequestDto.ItemDto> itemDtos = items.stream()
                .map(item -> ItemRequestDto.ItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .requestId(item.getRequestId())
                        .ownerId(item.getOwner().getId())
                        .build())
                .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}
