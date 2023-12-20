package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void addLike(int filmId, int userId) {
        checkFilmAndUserId(filmId, userId);
        Film film = filmStorage.findById(filmId);
        if (film == null) {
            throw new DoesNotExistException("Запрошенный фильм не найден");
        }
        film.getLikes().add(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        checkFilmAndUserId(filmId, userId);
        Film film = filmStorage.findById(filmId);
        if (film == null) {
            throw new DoesNotExistException("Запрошенный фильм не найден");
        }
        film.getLikes().remove(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> findTopLiked(int count) {
        if (filmStorage.findAll().isEmpty()) {
            throw new DoesNotExistException("Фильмов в базе пока нет");
        }
        return filmStorage.findAll().stream()
                .sorted((f0, f1) -> -1 * (f0.getLikes().size() - f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Genre> findAllGenres() {
        return filmStorage.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return filmStorage.findGenreById(id);
    }

    public List<Mpa> findAllMpa() {
        return filmStorage.findAllMpa();
    }

    public Mpa findMpaById(int id) {
        return filmStorage.findMpaById(id);
    }

    private void checkFilmAndUserId(int filmId, int userId) {
        if (filmId < 1 || userId < 1) {
            throw new DoesNotExistException("Введен не существующий идентификатор фильма либо пользователя");
        }
    }
}