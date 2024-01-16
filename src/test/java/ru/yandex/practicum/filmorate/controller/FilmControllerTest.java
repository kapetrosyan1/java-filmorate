package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage()));
    }

    @Test
    void createFilmTest() {
        Film film1 = new Film(1, "TestFilm1", "TestFilm1Description",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null);

        assertThrows(AlreadyExistException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");

        Film film = filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null));


        assertEquals(1, filmController.findAll().size());
        assertEquals(film, filmController.findAll().get(0));
    }

    @Test
    void filmValidationTest() {
        Film film2 = new Film(0, null, "TestFilm2Description",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null);
        Film film3 = new Film(0, "TestFilm3", "TestFilm3Description",
                LocalDate.of(1815, 11, 3), 120, null, new HashSet<>(), null);
        Film film4 = new Film(0, "TestFilm4", "TestFilm4Description",
                LocalDate.of(1996, 11, 3), 0, null, new HashSet<>(), null);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film4), "Должен выбросить исключение");
    }

    @Test
    void updateFilmTest() {
        filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null));
        Film nonUpdatable = new Film(4, "Non Updatable", "TestFilmDescription",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null);

        assertThrows(DoesNotExistException.class, () -> filmController.updateFilm(nonUpdatable), "Должен выбросить исключение");

        Film updatedFilm = filmController.updateFilm(new Film(1, "TestFilm", "TestFilmDescription",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null));

        assertEquals(1, filmController.findAll().size(), "Должен быть только 1 фильм");
        assertEquals(updatedFilm, filmController.findAll().get(0));
    }

    @Test
    void findAllTest() {
        List<Film> emptyFilmList = filmController.findAll();

        assertTrue(emptyFilmList.isEmpty(), "Фильмы пока не были добавлены");

        Film film1 = filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(1996, 11, 3), 120, null, new HashSet<>(), null));
        assertEquals(1, filmController.findAll().size(), "Неверный размер списка фильмов");
        assertEquals(film1, filmController.findAll().get(0), "Сохранен не тот фильм");
    }
}