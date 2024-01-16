package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return new ArrayList<>(userStorage.findAll());
    }

    public User findById(int id) {
        return userStorage.findById(id);
    }

    public List<User> findUserFriends(int userId) {
        List<User> friends = new ArrayList<>();

        for (int id : userStorage.findById(userId).getFriends()) {
            friends.add(userStorage.findById(id));
        }

        return friends;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        User user1 = userStorage.findById(userId);
        User user2 = userStorage.findById(friendId);

        if (user1 == null || user2 == null) {
            throw new DoesNotExistException("Пользователь с запрошенным идентификатором не найден");
        }

        user1.getFriends().add(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user1 = userStorage.findById(userId);
        User user2 = userStorage.findById(friendId);

        if (user1 == null || user2 == null) {
            throw new DoesNotExistException("Пользователь с запрошенным идентификатором не найден");
        }

        if (!user1.getFriends().contains(friendId)) {
            throw new DoesNotExistException("У пользователя с id " + userId + " нет в друзьях пользователя с id " + friendId);
        }

        user1.getFriends().remove(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> findMutualFriends(int user1Id, int user2Id) {
        Set<Integer> user1Friends = userStorage.findById(user1Id).getFriends();
        Set<Integer> user2Friends = userStorage.findById(user2Id).getFriends();

        if (user1Friends.isEmpty() || user2Friends.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> mutualFriendsId = user1Friends.stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toList());

        List<User> mutualFriends = new ArrayList<>();
        for (int id : mutualFriendsId) {
            mutualFriends.add(userStorage.findById(id));
        }
        return mutualFriends;
    }
}