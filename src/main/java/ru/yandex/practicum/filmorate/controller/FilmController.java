package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);
    private int filmNextId = 1;

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
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

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запущен метод по обновлению фильма. Текущее количество фильмов в базе: {}", films.size());
        if (!films.containsKey(film.getId())) {
            log.info("Выполнение метода прервано: фильм с id {} не найден. " +
                    "Текущее количество фильмов в базе: {}", film.getId(), films.size());
            throw new DoesNotExistException("Запрошенный для обновления фильм не найден");
        }
        filmValidationTest(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} был успешно обновлен. Текущее количество фильмов в базе: {}",
                film.getId(), films.size());
        return film;
    }

    private void filmValidationTest(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Выполнение метода прервано. Ошибка названия фильма. Название фильма: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.info("Выполнение метода прервано. Ошибка описания фильма. Текущая длина описания: {}",
                    film.getDescription().length());
            throw new ValidationException("Превышена максимально допустимая длина описания фильма");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("Выполнение метода прервано. Ошибка даты релиза фильма. Указанная дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Ошибка даты релиза фильма");
        } else if (film.getDuration() <= 0) {
            log.info("Выполнение метода прервано. Ошибка продолжительности фильма. Указана продолжительность: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
