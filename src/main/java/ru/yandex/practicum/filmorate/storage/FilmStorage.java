package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.List;

public interface FilmStorage {
    public List<Film> findAll();

    public Film create(Film film);

    public Film update(Film film);

    public Film findById(int id);
}