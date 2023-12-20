package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindFilmById() {
        // Подготавливаем данные для теста
        Film newFilm = new Film();
        newFilm.setId(0);
        newFilm.setName("New Film");
        newFilm.setDescription("New Film description");
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        newFilm.setMpa(mpa);
        newFilm.setDuration(100);
        newFilm.setReleaseDate(LocalDate.of(1990, 1, 1));

        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        filmStorage.create(newFilm);

        // вызываем тестируемый метод
        Film savedFilm = filmStorage.findById(1);

        // проверяем утверждения
        assertThat(savedFilm)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(newFilm);        // и сохраненного пользователя - совпадают
    }

    @Test
    public void testFindAllMpa() {
        FilmStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        List<Mpa> mpa = filmStorage.findAllMpa();

        assertEquals(5, mpa.size());
    }

    @Test
    public void testFindMpaById() {
        FilmStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        Mpa mpa = new Mpa();
        mpa.setId(3);
        mpa.setName("PG-13");

        Mpa savedMpa = filmStorage.findMpaById(3);

        assertEquals(mpa, savedMpa);
    }


}