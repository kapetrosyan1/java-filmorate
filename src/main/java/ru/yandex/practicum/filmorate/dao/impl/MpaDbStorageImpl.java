package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
public class MpaDbStorageImpl implements MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY mpa_id";
        return jdbcTemplate.query(sql, mpaRowMapper());
    }

    @Override
    public Mpa findById(int id) {
        try {
            String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
            return jdbcTemplate.queryForObject(sql, mpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistException("Возрастной рейтинг с идентификатором " + id + " не найден");
        }
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        };
    }
}