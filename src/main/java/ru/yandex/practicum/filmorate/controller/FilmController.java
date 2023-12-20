package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping
public class FilmController {

    private final FilmService service;

    public FilmController(FilmService service) {
        this.service = service;
    }

    @GetMapping("/films")
    public List<Film> findAll() {
        return service.findAll();
    }

    @GetMapping("/films/{id}")
    public Film findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/films/popular")
    public List<Film> findTopLiked(@RequestParam(defaultValue = "10") Integer count) {
        if (count <= 0) {
            throw new ValidationException("Значение параметра count должно быть больше нуля");
        }
        return service.findTopLiked(count);
    }

    @GetMapping("/genres")
    public List<Genre> findAllGenres() {
        return service.findAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre findGenreById(@PathVariable Integer id) {
        return service.findGenreById(id);
    }

    @GetMapping("/mpa")
    public List<Mpa> findAllMpa() {
        return service.findAllMpa();
    }

    @GetMapping("/mpa/{id}")
    public Mpa findMpaById(@PathVariable Integer id) {
        return service.findMpaById(id);
    }

    @PostMapping("/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        return service.create(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return service.update(film);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id, userId);
    }
}