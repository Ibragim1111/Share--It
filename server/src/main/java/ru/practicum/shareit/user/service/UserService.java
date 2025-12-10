package ru.practicum.shareit.user.service;


import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UpdateUserDto;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, UpdateUserDto user);

    void deleteUser(Long id);
}
