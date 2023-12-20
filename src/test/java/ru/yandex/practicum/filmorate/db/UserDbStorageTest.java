package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        // Подготавливаем данные для теста
        User newUser = new User();
        newUser.setId(0);
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);

        // вызываем тестируемый метод
        User savedUser = userStorage.findById(1);

        // проверяем утверждения
        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(newUser);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testFindAll() {
        User newUser = new User();
        newUser.setId(0);
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));


        User user1 = new User();
        user1.setId(0);
        user1.setEmail("user123@email.ru");
        user1.setLogin("Ivan123");
        user1.setName("Ivan Petrov");
        user1.setBirthday(LocalDate.of(1992, 1, 1));

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);
        userStorage.create(user1);

        assertEquals(2, userStorage.findAll().size());
        assertEquals(newUser, userStorage.findById(1));
        assertEquals(user1, userStorage.findById(2));
    }
    @Test
    public void testUpdateUser() {
        User newUser = new User();
        newUser.setId(0);
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);

        User user1 = new User();
        user1.setId(2);
        user1.setEmail("user@email.ru");
        user1.setLogin("Ivan123");
        user1.setName("Ivan Petrov");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        userStorage.update(user1);

        assertEquals(user1, userStorage.findById(1));
        assertEquals(1, userStorage.findAll().size());
    }

    @Test
    public void testFindUserFriends() {
        User newUser = new User();
        newUser.setId(0);
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User user1 = new User();
        user1.setId(0);
        user1.setEmail("user123@email.ru");
        user1.setLogin("Ivan123");
        user1.setName("Ivan Petrov");
        user1.setBirthday(LocalDate.of(1992, 1, 1));

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);
        userStorage.create(user1);

        userStorage.addFriend(1, 2);

        newUser = userStorage.findById(1);
        user1 = userStorage.findById(2);

        assertEquals(Set.of(2), newUser.getFriends());
        assertEquals(Set.of(), user1.getFriends());

        userStorage.addFriend(2, 1);
        newUser = userStorage.findById(1);
        user1 = userStorage.findById(2);

        assertEquals(Set.of(1), user1.getFriends());
        assertEquals(Set.of(2), newUser.getFriends());
    }

    @Test
    public void testRemoveFriend() {
        User newUser = new User();
        newUser.setId(0);
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User user1 = new User();
        user1.setId(0);
        user1.setEmail("user123@email.ru");
        user1.setLogin("Ivan123");
        user1.setName("Ivan Petrov");
        user1.setBirthday(LocalDate.of(1992, 1, 1));

        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(newUser);
        userStorage.create(user1);
        userStorage.addFriend(1, 2);
        userStorage.addFriend(2, 1);

        userStorage.removeFriend(1, 2);

        newUser = userStorage.findById(1);
        user1 = userStorage.findById(2);

        assertEquals(Set.of(), newUser.getFriends());
        assertEquals(Set.of(1), user1.getFriends());

        userStorage.removeFriend(2, 1);

        newUser = userStorage.findById(1);
        user1 = userStorage.findById(2);

        assertEquals(0, newUser.getFriends().size());
        assertEquals(0, user1.getFriends().size());
    }
}