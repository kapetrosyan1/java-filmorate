package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
    }

    @Test
    void createUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("usermail@gmail.com");
        user.setLogin("userLogin");
        user.setName("User Userovich");
        user.setBirthday(LocalDate.of(1996, 11, 3));

        assertThrows(AlreadyExistException.class, () -> userController.createUser(user), "Должен выбросить исключение");

        User user1 = new User();
        user1.setId(0);
        user1.setEmail("usermail1@gmail.com");
        user1.setLogin("user1Login");
        user1.setName("User Userovich");
        user1.setBirthday(LocalDate.of(1996, 11, 3));

        user1 = userController.createUser(user1);

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(user1, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void userValidationTest() {
        User user1 = new User();
        user1.setId(0);
        user1.setEmail(null);
        user1.setLogin("userLogin");
        user1.setName("User Userovich");
        user1.setBirthday(LocalDate.of(1996, 11, 3));

        User user2 = new User();
        user2.setId(0);
        user2.setEmail("usermailgmail.com");
        user2.setLogin("userLogin");
        user2.setName("User Userovich");
        user2.setBirthday(LocalDate.of(1996, 11, 3));

        User user3 = new User();
        user3.setId(0);
        user3.setEmail("usermail@gmail.com");
        user3.setLogin(null);
        user3.setName("User Userovich");
        user3.setBirthday(LocalDate.of(1996, 11, 3));

        User user4 = new User();
        user4.setId(0);
        user4.setEmail("usermail@gmail.com");
        user4.setLogin("user Login");
        user4.setName("User Userovich");
        user4.setBirthday(LocalDate.of(1996, 11, 3));

        User user5 = new User();
        user5.setId(0);
        user5.setEmail("usermail@gmail.com");
        user5.setLogin("userLogin");
        user5.setName("User Userovich");
        user5.setBirthday(LocalDate.of(2024, 11, 3));

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user4), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user5), "Должен выбросить исключение");
    }

    @Test
    void userUpdateTest() {
        User user1 = new User();
        user1.setId(0);
        user1.setEmail("usermail1@gmail.com");
        user1.setLogin("user1Login");
        user1.setName("User Userovich");
        user1.setBirthday(LocalDate.of(1996, 11, 3));
        userController.createUser(user1);

        User nonUpdatable = new User();
        nonUpdatable.setId(0);
        nonUpdatable.setEmail("updatedUsermail1@gmail.com");
        nonUpdatable.setLogin("updatedUser1Login");
        nonUpdatable.setName("NewUser Userovich");
        nonUpdatable.setBirthday(LocalDate.of(1996, 11, 3));

        assertThrows(DoesNotExistException.class, () -> userController.updateUser(nonUpdatable), "Должен выбросить исключение");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("updatedUsermail1@gmail.com");
        updatedUser.setLogin("updatedUser1Login");
        updatedUser.setName("NewUser Userovich");
        updatedUser.setBirthday(LocalDate.of(1996, 11, 3));
        userController.updateUser(updatedUser);

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(updatedUser, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void findAllTest() {
        List<User> emptyUserList = userController.findAll();

        assertTrue(emptyUserList.isEmpty(), "Пользователи пока не были добавлены");
        User user1 = new User();
        user1.setId(0);
        user1.setEmail("usermail1@gmail.com");
        user1.setLogin("user1Login");
        user1.setName("User Userovich");
        user1.setBirthday(LocalDate.of(1996, 11, 3));

        user1 = userController.createUser(user1);

        assertEquals(1, userController.findAll().size(), "Неверный размер списка пользователей");
        assertEquals(user1, userController.findAll().get(0), "Сохранен не тот пользователь");
    }
}