package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser() {
        User user = new User(1, "usermail@gmail.com", "userLogin",
                "User Userovich", LocalDate.of(1996, 11, 3));

        assertThrows(AlreadyExistException.class, () -> userController.createUser(user), "Должен выбросить исключение");

        User user1 = userController.createUser(new User(0, "usermail1@gmail.com", "user1Login",
                "User Userovich", LocalDate.of(1996, 11, 3)));

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(user1, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void userValidationTest() {
        User user1 = new User(0, null, "userLogin",
                "User Userovich", LocalDate.of(1996, 11, 3));
        User user2 = new User(0, "usermailgmail.com", "userLogin",
                "User Userovich", LocalDate.of(1996, 11, 3));
        User user3 = new User(0, "usermail@gmail.com", null,
                "User Userovich", LocalDate.of(1996, 11, 3));
        User user4 = new User(0, "usermail@gmail.com", "user Login",
                "User Userovich", LocalDate.of(1996, 11, 3));
        User user5 = new User(0, "usermail@gmail.com", "userLogin",
                "User Userovich", LocalDate.of(2024, 11, 3));

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user4), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user5), "Должен выбросить исключение");
    }

    @Test
    void userUpdateTest() {
        User user1 = userController.createUser(new User(0, "usermail1@gmail.com", "user1Login",
                "User Userovich", LocalDate.of(1996, 11, 3)));

        User nonUpdatabble = new User(0, "updatedUsermail1@gmail.com", "updatedUser1Login",
                "NewUser Userovich", LocalDate.of(1996, 11, 3));

        assertThrows(DoesNotExistException.class, () -> userController.updateUser(nonUpdatabble), "Должен выбросить исключение");

        User updatedUser = userController.updateUser(new User(1, "updatedUsermail1@gmail.com", "updatedUser1Login",
                "NewUser Userovich", LocalDate.of(1996, 11, 3)));

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(updatedUser, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void findAllTest() {
        List<User> emptyUserList = userController.findAll();

        assertTrue(emptyUserList.isEmpty(), "Пользователи пока не были добавлены");

        User user1 = userController.createUser(new User(0, "usermail1@gmail.com", "user1Login",
                "User Userovich", LocalDate.of(1996, 11, 3)));

        assertEquals(1, userController.findAll().size(), "Неверный размер списка пользователей");
        assertEquals(user1, userController.findAll().get(0), "Сохранен не тот пользователь");
    }
}
