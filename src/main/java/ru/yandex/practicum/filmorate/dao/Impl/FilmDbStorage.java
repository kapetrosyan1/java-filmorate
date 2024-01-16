package ru.yandex.practicum.filmorate.dao.Impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        mpaDbStorage = new MpaDbStorageImpl(jdbcTemplate);
        genreDbStorage = new GenreDbStorageImpl(jdbcTemplate);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> filmList = jdbcTemplate.query(sql, filmRowMapper());
        for (Film film : filmList) {
            Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
            genres.addAll(genreDbStorage.findByFilmId(film.getId()));
            film.setGenres(genres);
            film.setLikes(new TreeSet<>(getFilmLikes(film.getId())));
        }
        return filmList;
    }

    @Override
    public Film create(Film film) {
        validateReleaseDate(film);
        if (film.getId() != 0) {
            throw new AlreadyExistException("Фильм уже был создан в базе");
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToRow(film)).intValue();
        film.setId(id);

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return film;
        }

        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        for (Genre genre : filmGenre) {
            genreDbStorage.addGenreToTheFilm(id, genre.getId());
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        validateReleaseDate(film);
        if (film.getId() == 0) {
            throw new DoesNotExistException("Запрошенного фильма не существует");
        }
        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ?" +
                " WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getMpa().getId(),
                film.getReleaseDate(), film.getDuration(), film.getId());
        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных пользователя произошла непредвиденная ошибка");
        }
        /*
        Код ниже в целом лишний, но тесты Postman жаловались на порядок выдачи жанров. Другого способа решить проблему не смог
         */
        if (film.getGenres() != null) {
            Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
            genres.addAll(film.getGenres());
            film.setGenres(genres);
        }

        genreDbStorage.updateFilmGenre(film);
        return film;
    }

    @Override
    public Film findById(int id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?";

            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
            if (film == null) {
                throw new UnexpectedException("Случилась непредвиденная ошибка - передан null");
            }
            Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
            genres.addAll(genreDbStorage.findByFilmId(id));
            film.setGenres(genres);

            List<Integer> likes = getFilmLikes(id);

            if (likes.isEmpty()) {
                return film;
            }

            film.setLikes(new TreeSet<>(likes));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void addLike(int userId, int filmId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        int update = jdbcTemplate.update(sql, filmId, userId);

        if (update == 0) {
            throw new UnexpectedException("Произошла непредвиденная ошибка при обновлении списка лайков");
        }
    }

    @Override
    public void removeLike(int userId, int filmId) {
        String sql = "DELETE FROM likes WHERE film_id=? AND user_id=?";
        int update = jdbcTemplate.update(sql, filmId, userId);
        if (update == 0) {
            throw new UnexpectedException("Произошла ошибка при удалении лайка");
        }
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> new Film(rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                mpaDbStorage.findById(rs.getInt("mpa_id")),
                new TreeSet<>(),
                new TreeSet<>(Comparator.comparing(Genre::getId)));
    }

    private List<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("user_id"), filmId);
    }

    private Map<String, Object> filmToRow(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("mpa_id", film.getMpa().getId());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        return values;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть после 28.12.1895");
        }
    }
}
