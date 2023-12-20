package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
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
        return jdbcTemplate.query(sql, userRowMapper());
    }

    @Override
    public User create(User user) {
        userValidationTest(user);
        if (user.getId() != 0) {
            throw new AlreadyExistException("Пользователь уже существует");
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        int id = simpleJdbcInsert.executeAndReturnKey(userToRow(user)).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        userValidationTest(user);
        if (user.getId() == 0) {
            throw new DoesNotExistException("Пользователь с таким id не существует");
        }
        findById(user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int updatedRows = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(),
                user.getId());

        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных пользователя произошла непредвиденная ошибка");
        }

        if (user.getFriends().isEmpty()) {
            return user;
        }

        List<Integer> friends = new ArrayList<>(user.getFriends());

        for (int friend : friends) {
            addUserFriend(user.getId(), friend);
        }
        return user;
    }

    @Override
    public User findById(int id) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            if (findUserFriends(id).isEmpty()) {
                return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            }

            User user = jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            user.setFriends(new HashSet<>(findUserFriends(id)));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Пользователь с id " + id + " не найден");
        }
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
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id=? AND friend_id=?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        };
    }

    private Map<String, Object> userToRow(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    private List<Integer> findUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("friend_id"), userId);
    }

    private void addUserFriend(int userId, int friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        int update = jdbcTemplate.update(sql, userId, friendId);
        if (update == 0) {
            throw new UnexpectedException("Произошла непредвиденная ошибка при добавлении друга");
        }
    }

    private void userValidationTest(User user) {
        emailValidation(user);
        loginValidation(user);
        birthdayValidation(user);
        setLoginAsNameIfBlankOrNull(user);
    }

    private void emailValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный адрес электронной почты");
        }
    }

    private void loginValidation(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }

    private void birthdayValidation(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Указана неправильная дата рождения");
        }
    }

    private void setLoginAsNameIfBlankOrNull(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}