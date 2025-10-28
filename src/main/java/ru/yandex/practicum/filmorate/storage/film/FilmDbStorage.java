package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        String sql = "INSERT INTO FILMS (name, description, release_date, duration) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());
        if (rows == 0) {
            throw new NotFoundException("Film with id " + film.getId() + " not found");
        }
        return film;
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = "SELECT * FROM FILMS WHERE id = ?";
        List<Film> films = jdbcTemplate.query(
                sql,
                this::mapRowToFilm,
                id
        );
        return films.stream().findFirst();
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILMS";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM FILMS WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);

        if (rows == 0) {
            throw new NotFoundException("Film with id " + id + " not found");
        }
    }

    @Override
    public Film addLike(long filmId, long userId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection<Film> findPopular(int count) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getObject("release_date", java.time.LocalDate.class))
                .duration(resultSet.getInt("duration"))
                .build();
    }
}
