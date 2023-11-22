package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private int userNextId = 1;

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User findById(int id) {
        if (!users.containsKey(id)) {
            throw new DoesNotExistException("Пользователь с идентификатором " + id + " не найден");
        }

        return users.get(id);
    }

    public User create(User user) {
        log.info("Запущен метод по добавлению пользователя. Текущее количество пользователей в базе: {}", users.size());
        if (user.getId() != 0) {
            log.info("Выполнение метода прервано: пользователю был присвоен id {} до его добавления. " + "Текущее количество пользователей в базе: {}", user.getId(), users.size());
            throw new AlreadyExistException("Фильм уже был создан");
        }
        userValidationTest(user);
        user.setId(userNextId);
        userNextId++;
        users.put(user.getId(), user);
        log.info("Пользователь с email {}, логином {} и id {} был успешно добавлен. Текущее количество " + "пользователей в базе: {}", user.getEmail(), user.getLogin(), user.getId(), users.size());
        return user;
    }

    public User update(User user) {
        log.info("Запущен метод по обновлению пользователя. Текущее количество пользователей в базе: {}", users.size());
        if (!users.containsKey(user.getId())) {
            log.info("Выполнение метода прервано: пользователь с id {} не зарегистрирован в базе. Текущее количество " + "пользователей в базе: {}", user.getId(), users.size());
            throw new DoesNotExistException("Пользователь не зарегистрирован");
        }
        userValidationTest(user);
        users.put(user.getId(), user);
        log.info("Данные пользователя с id {} были успешно обновлены. Текущее количество " + "пользователей в базе: {}", user.getId(), users.size());
        return user;
    }

    private void userValidationTest(User user) {
        emailValidation(user);
        loginValidation(user);
        birthdayValidation(user);
        setLoginAsNameIfBlankOrNull(user);
    }

    private void emailValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.info("Выполнение метода прервано: некорректный email. Указанный email: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты");
        }
    }

    private void loginValidation(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Выполнение метода прервано: некорректный логин. Указанный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }

    private void birthdayValidation(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Выполнение метода прервано: некорректная дата рождения пользователя. Указанная дата: {}", user.getBirthday());
            throw new ValidationException("Указана неправильная дата рождения");
        }
    }

    private void setLoginAsNameIfBlankOrNull(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователь не указал имя. Имени присвоено значение логина {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}