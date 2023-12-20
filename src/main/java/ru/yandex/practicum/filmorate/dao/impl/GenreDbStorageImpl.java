package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Component
public class GenreDbStorageImpl implements GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, mapToGenre());
    }

    @Override
    public Genre findById(int id) {
        try {
            String sql = "SELECT * FROM genres WHERE genre_id=?";
            return jdbcTemplate.queryForObject(sql, mapToGenre(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Жанр с запрошенным идентификатором не найден");
        }
    }

    @Override
    public void addGenreToTheFilm(int filmId, int genreId) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        int updatedRow = jdbcTemplate.update(sql, filmId, genreId);
        if (updatedRow != 1) {
            throw new UnexpectedException("Произошла ошибка при добавлении жанра");
        }
    }

    @Override
    public List<Genre> findByFilmId(int filmId) {
        List<Integer> idList = findIdsByFilm(filmId);
        List<Genre> genres = new ArrayList<>();
        for (int id : idList) {
            Genre genre = new Genre();
            genre.setId(id);
            genre.setName(findById(id).getName());
            genres.add(genre);
        }
        return genres;
    }
    @Override
    public void updateFilmGenre(Film film) {
        String sql = "DELETE FROM film_genre WHERE film_id=?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        for (Genre genre : filmGenre) {
            addGenreToTheFilm(film.getId(), genre.getId());
        }
    }

    private List<Integer> findIdsByFilm(int filmId) {
        String sql = "SELECT * FROM film_genre WHERE film_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("genre_id"), filmId);
    }

    private RowMapper<Genre> mapToGenre() {
        return (rs, rowNum) -> {
            Genre genre = new Genre();

            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        };
    }
}