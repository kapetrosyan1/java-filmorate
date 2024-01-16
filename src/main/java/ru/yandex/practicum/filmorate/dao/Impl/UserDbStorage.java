package ru.yandex.practicum.filmorate.dao.Impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Component
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> userList = jdbcTemplate.query(sql, userRowMapper());
        for (User user : userList) {
            user.setFriends(new HashSet<>(findUserFriends(user.getId())));
        }
        return userList;
    }

    @Override
    public User create(User user) {
        if (user.getId() != 0) {
            throw new AlreadyExistException("Пользователь уже существует");
        }
        setLoginAsNameIfBlankOrNull(user);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        int id = simpleJdbcInsert.executeAndReturnKey(userToRow(user)).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        int update = jdbcTemplate.update(sql, userId, friendId);

        if (update == 0) {
            throw new UnexpectedException("При обновлении списка друзей произошла непредвиденная ошибка");
        }
    }

    @Override
    public User findById(int id) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            if (findUserFriends(id).isEmpty()) {
                return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            }

            User user = jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            if (user == null) {
                throw new UnexpectedException("Произошла непредвиденная ошибка при создании пользователя");
            }
            user.setFriends(new TreeSet<>(findUserFriends(id)));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public User update(User user) {
        if (user.getId() == 0) {
            throw new DoesNotExistException("Пользователь с таким id не существует");
        }
        setLoginAsNameIfBlankOrNull(user);

        findById(user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int updatedRows = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(),
                user.getId());

        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных пользователя произошла непредвиденная ошибка");
        }
        return user;
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id=? AND friend_id=?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                new HashSet<>()
        );
    }

    private Map<String, Object> userToRow(User user) {
        return new HashMap<>(Map.of("email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday()));
    }

    private List<Integer> findUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("friend_id"), userId);
    }

    private void setLoginAsNameIfBlankOrNull(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}