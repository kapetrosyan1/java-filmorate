package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
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
        Film film1 = new Film();
        film1.setId(1);
        film1.setName("TestFilm1");
        film1.setDescription("TestFilm1Description");
        film1.setReleaseDate(LocalDate.of(1996, 11, 3));
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        film1.setMpa(mpa);
        film1.setDuration(120);

        assertThrows(AlreadyExistException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");
        Film testFilm = new Film();
        testFilm.setName("TestFilm1");
        testFilm.setDescription("TestFilm1Description");
        testFilm.setReleaseDate(LocalDate.of(1996, 11, 3));
        testFilm.setMpa(mpa);
        testFilm.setDuration(120);

        Film film = filmController.createFilm(testFilm);


        assertEquals(1, filmController.findAll().size());
        assertEquals(film, filmController.findAll().get(0));
    }

    @Test
    void filmValidationTest() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");

        Film film2 = new Film();
        film2.setId(0);
        film2.setName(null);
        film2.setDescription("TestFilm2Description");
        film2.setReleaseDate(LocalDate.of(1996, 11, 3));
        film2.setMpa(mpa);
        film2.setDuration(120);

        Film film3 = new Film();
        film3.setId(0);
        film3.setName("TestFilm3");
        film3.setDescription("TestFilm3Description");
        film3.setReleaseDate(LocalDate.of(1815, 11, 3));
        film3.setMpa(mpa);
        film3.setDuration(120);

        Film film4 = new Film();
        film4.setId(0);
        film4.setName("TestFilm4");
        film4.setDescription("TestFilm4Description");
        film4.setReleaseDate(LocalDate.of(1996, 11, 3));
        film4.setMpa(mpa);
        film4.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film4), "Должен выбросить исключение");
    }

    @Test
    void updateFilmTest() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");

        Film film = new Film();
        film.setId(0);
        film.setName("TestFilm");
        film.setDescription("TestFilmDescription");
        film.setReleaseDate(LocalDate.of(1996, 11, 3));
        film.setMpa(mpa);
        film.setDuration(120);
        filmController.createFilm(film);

        Film nonUpdatable = new Film();
        film.setId(4);
        film.setName("Non Updatable");
        film.setDescription("TestFilmDescription");
        film.setReleaseDate(LocalDate.of(1996, 11, 3));
        film.setMpa(mpa);
        film.setDuration(120);


        assertThrows(DoesNotExistException.class, () -> filmController.updateFilm(nonUpdatable), "Должен выбросить исключение");

        Film forUpdate = new Film();
        film.setId(1);
        film.setName("TestFilm1");
        film.setDescription("TestFilmDescription");
        film.setReleaseDate(LocalDate.of(1996, 11, 3));
        film.setMpa(mpa);
        film.setDuration(120);

        Film updatedFilm = filmController.updateFilm(forUpdate);

        assertEquals(1, filmController.findAll().size(), "Должен быть только 1 фильм");
        assertEquals(updatedFilm, filmController.findAll().get(0));
    }

    @Test
    void findAllTest() {
        List<Film> emptyFilmList = filmController.findAll();

        assertTrue(emptyFilmList.isEmpty(), "Фильмы пока не были добавлены");
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");

        Film film = new Film();
        film.setId(0);
        film.setName("TestFilm");
        film.setDescription("TestFilmDescription");
        film.setReleaseDate(LocalDate.of(1996, 11, 3));
        film.setMpa(mpa);
        film.setDuration(120);

        film = filmController.createFilm(film);
        assertEquals(1, filmController.findAll().size(), "Неверный размер списка фильмов");
        assertEquals(film, filmController.findAll().get(0), "Сохранен не тот фильм");
    }
}