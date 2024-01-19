package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(User user);

    User findById(int id);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);
}