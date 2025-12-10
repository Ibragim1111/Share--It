package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(NewUserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public static User updateUserFields(User user, UpdateUserDto request) {

            User updatedUser = new User();
            updatedUser.setId(user.getId());
            updatedUser.setName(request.getName() != null ? request.getName() : user.getName());
            updatedUser.setEmail(request.getEmail() != null ? request.getEmail() : user.getEmail());

            return updatedUser;

    }
}
