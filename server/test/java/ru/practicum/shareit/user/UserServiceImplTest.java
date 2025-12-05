package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.exception.ConflictException;
import ru.practicum.shareit.exceptions.exception.NotFoundException;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .build();
    }

    @Test
    void createUser_whenValid_thenSuccess() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_whenEmailExists_thenThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class,
                () -> userService.createUser(user));
    }

    @Test
    void getUserById_whenExists_thenSuccess() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void getUserById_whenNotExists_thenThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_thenSuccess() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateUser_whenValid_thenSuccess() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenEmailConflict_thenThrowException() {
        User existingUser = User.builder()
                .id(2L)
                .name("Other User")
                .email("other@email.com")
                .build();

        UpdateUserDto updateDto = UpdateUserDto.builder()
                .email("other@email.com")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        assertThrows(ConflictException.class,
                () -> userService.updateUser(1L, updateDto));
    }

    @Test
    void deleteUser_thenSuccess() {
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
