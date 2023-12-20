package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Film findById(int id);

    List<Genre> findAllGenres();

    Genre findGenreById(int id);

    List<Mpa> findAllMpa();

    Mpa findMpaById(int id);

    void addLike(int userId, int filmId);
    void removeLike(int userId, int filmId);

}