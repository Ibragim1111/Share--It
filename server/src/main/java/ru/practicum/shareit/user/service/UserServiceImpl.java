package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.exception.ConflictException;
import ru.practicum.shareit.exceptions.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, UpdateUserDto updateUser) {
        User existingUser = getUserById(id);

        User user = UserMapper.updateUserFields(existingUser,updateUser);

        if (!user.getEmail().equals(existingUser.getEmail())) {
            if (isEmailExists(user.getEmail(), id)) {
                throw new ConflictException("Email already exists: " + user.getEmail());
            }
        }



        return userRepository.save(user);
    }


    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private boolean isEmailExists(String email, Long excludeUserId) {
        return userRepository.findAll().stream()
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .anyMatch(u -> u.getEmail().equals(email));
    }
}
