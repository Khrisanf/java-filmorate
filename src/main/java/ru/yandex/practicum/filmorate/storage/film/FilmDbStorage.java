package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("dbFilms")
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
        assertFilmExists(filmId);
        assertUserExists(userId);

        final String sql = """
        INSERT INTO likes (film_id, user_id)
        SELECT ?, ?
        WHERE NOT EXISTS (
            SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?
        )
        """;

        jdbcTemplate.update(sql, filmId, userId, filmId, userId);

        return findById(filmId).orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
    }


    @Override
    public Film removeLike(long filmId, long userId) {
        assertFilmExists(filmId);
        assertUserExists(userId);
        final String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        return findById(filmId).orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
    }

    @Override
    public Collection<Film> findPopular(int count) {
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       COUNT(l.user_id) AS likes_cnt
                FROM films f
                LEFT JOIN likes l ON l.film_id = f.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration
                ORDER BY likes_cnt DESC, f.id
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }


    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getObject("release_date", java.time.LocalDate.class))
                .duration(resultSet.getInt("duration"))
                .likes(new HashSet<>())
                .build();
    }

    private void assertUserExists(long userId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)", Boolean.class, userId
        );
        if (exists == null || !exists) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }

    private void assertFilmExists(long filmId) {
        if (findById(filmId).isEmpty()) {
            throw new NotFoundException("Film with id " + filmId + " not found");
        }
    }
}
