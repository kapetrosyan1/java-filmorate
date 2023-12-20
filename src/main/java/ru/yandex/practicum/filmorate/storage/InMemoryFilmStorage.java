package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private int filmNextId = 1;

    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    public Film findById(int id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new DoesNotExistException("Фильм не найден");
    }

    public Film create(Film film) {
        log.info("Запущен метод по добавлению фильма. Текущее количество фильмов в базе: {}", films.size());
        if (film.getId() != 0) {
            log.info("Выполнение метода прервано: фильму был присвоен id {} до его добавления. " +
                    "Текущее количество фильмов в базе: {}", film.getId(), films.size());
            throw new AlreadyExistException("Фильм уже добавлен");
        }
        filmValidationTest(film);
        film.setId(filmNextId);
        filmNextId++;
        films.put(film.getId(), film);
        log.info("Фильм с названием {} и id {} был успешно добавлен. Текущее количество фильмов в базе: {}",
                film.getName(), film.getId(), films.size());
        return film;
    }

    public Film update(Film film) {
        log.info("Запущен метод по обновлению фильма. Текущее количество фильмов в базе: {}", films.size());
        if (!films.containsKey(film.getId())) {
            log.info("Выполнение метода прервано: фильм с id {} не найден. " + "Текущее количество фильмов в базе: {}",
                    film.getId(), films.size());
            throw new DoesNotExistException("Запрошенный для обновления фильм не найден");
        }
        filmValidationTest(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} был успешно обновлен. Текущее количество фильмов в базе: {}", film.getId(), films.size());
        return film;
    }

    @Override
    public List<Genre> findAllGenres() {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    @Override
    public Genre findGenreById(int id) {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    @Override
    public List<Mpa> findAllMpa() {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    @Override
    public Mpa findMpaById(int id) {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    @Override
    public void addLike(int userId, int filmId) {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    @Override
    public void removeLike(int userId, int filmId) {
        throw new UnexpectedException("Данный метод еще не определен");
    }

    private void filmValidationTest(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
        log.info("Фильм успешно прошел валидацию");
    }

    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Выполнение метода прервано. Ошибка названия фильма. Название фильма: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription().length() > 200) {
            log.info("Выполнение метода прервано. Ошибка описания фильма. Текущая длина описания: {}",
                    film.getDescription().length());
            throw new ValidationException("Превышена максимально допустимая длина описания фильма");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("Выполнение метода прервано. Ошибка даты релиза фильма. Указанная дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Ошибка даты релиза фильма");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() <= 0) {
            log.info("Выполнение метода прервано. Ошибка продолжительности фильма. Указана продолжительность: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}