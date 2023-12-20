package ru.yandex.practicum.filmorate.dao.impl;

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
            film.setGenres(new HashSet<>(genreDbStorage.findByFilmId(film.getId())));
            film.setLikes(new HashSet<>(getFilmLikes(film.getId())));
        }
        return filmList;
    }

    @Override
    public Film create(Film film) {
        if (film.getId() != 0) {
            throw new AlreadyExistException("Фильм уже есть в базе");
        }
        filmValidationTest(film);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToRow(film)).intValue();
        film.setId(id);

        if (film.getGenres().isEmpty()) {
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
        if (film.getId() == 0) {
            throw new DoesNotExistException("Запрошенного фильма не существует");
        }
        filmValidationTest(film);
        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ?" +
                " WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getMpa().getId(),
                film.getReleaseDate(), film.getDuration(), film.getId());
        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных пользователя произошла непредвиденная ошибка");
        }

        genreDbStorage.updateFilmGenre(film);
        return film;
    }

    @Override
    public Film findById(int id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?";

            if (genreDbStorage.findByFilmId(id).isEmpty()) {
                return jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
            }

            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
            if (film == null) {
                throw new UnexpectedException("Случилась непредвиденная ошибка");
            }
            film.setGenres(new HashSet<>(genreDbStorage.findByFilmId(id)));

            List<Integer> likes = getFilmLikes(id);

            if (likes.isEmpty()) {
                return film;
            }

            film.setLikes(new HashSet<>(likes));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        return genreDbStorage.findAll();
    }

    @Override
    public Genre findGenreById(int id) {
        return genreDbStorage.findById(id);
    }

    @Override
    public List<Mpa> findAllMpa() {
        return mpaDbStorage.findAll();
    }

    @Override
    public Mpa findMpaById(int id) {
        return mpaDbStorage.findById(id);
    }

    @Override
    public void addLike(int userId, int filmId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        int update = jdbcTemplate.update(sql, userId, filmId);

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

    private List<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("user_id"), filmId);
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setMpa(findMpaById(rs.getInt("mpa_id")));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            return film;
        };
    }

    private void filmValidationTest(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
    }

    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Превышена максимально допустимая длина описания фильма");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка даты релиза фильма");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
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
}