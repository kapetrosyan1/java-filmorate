package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService service;

    public FilmController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public List<Film> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/popular")
    public List<Film> findTopLiked(@RequestParam(defaultValue = "10") Integer count) {
        if (count <= 0) {
            throw new ValidationException("Значение параметра count должно быть больше нуля");
        }
        return service.findTopLiked(count);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return service.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return service.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id, userId);
    }
}