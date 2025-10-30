package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Profile("dbFilms")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            assertMpaExists(film.getMpa().getId());
        }

        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa_rating_id) VALUES (?,?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setObject(5, null);
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        upsertFilmGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        assertFilmExists(film.getId());
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            assertMpaExists(film.getMpa().getId());
        }

        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id=? WHERE id=?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        if (rows == 0) {
            throw new NotFoundException("Film with id " + film.getId() + " not found");
        }

        upsertFilmGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = "SELECT id, " +
                "name, description, " +
                "release_date, duration, " +
                "mpa_rating_id FROM films WHERE id=?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rn) -> mapRowToFilm(rs), id);
        Optional<Film> optionalFilm = films.stream().findFirst();
        optionalFilm.ifPresent(this::fillMpaAndGenres);
        return optionalFilm;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT id, name, description," +
                " release_date, duration, mpa_rating_id FROM films";
        List<Film> films = jdbcTemplate.query(sql, (rs, ns) -> mapRowToFilm(rs));
        films.forEach(this::fillMpaAndGenres);
        return films;
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
        List<Film> films = jdbcTemplate.query(sql, (rs, rn) -> mapRowToFilm(rs), count);
        films.forEach(this::fillMpaAndGenres);
        return films;
    }


    private Film mapRowToFilm(ResultSet resultSet) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getObject("release_date", LocalDate.class))
                .duration(resultSet.getInt("duration"))
                .likes(new HashSet<>())
                .build();
    }

    private void fillMpaAndGenres(Film film) {
        Integer mpaId = jdbcTemplate.queryForObject(
                "SELECT mpa_rating_id FROM films WHERE id=?",
                Integer.class, film.getId());
        if (mpaId != null) {
            film.setMpa(mpaStorage.findById(mpaId).orElse(null));
        }

        List<Genre> genres = jdbcTemplate.query("""
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """, (rs, rn) -> Genre.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .build(),
                film.getId()
        );

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void upsertFilmGenres(long filmId, Set<Genre> input) {
        List<Integer> ids = (input == null)
                ? Collections.emptyList()
                : input.stream()
                .filter(g -> g != null && g.getId() != null)
                .map(Genre::getId)
                .distinct()
                .toList();

        if (!ids.isEmpty()) {
            String inClause = ids.stream().map(x -> "?").collect(Collectors.joining(","));
            Integer found = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM genres WHERE id IN (" + inClause + ")",
                    Integer.class,
                    ids.toArray()
            );
            if (found == null || found != ids.size()) {
                throw new NotFoundException("One or more genres not found");
            }
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (!ids.isEmpty()) {
            List<Object[]> batch = ids.stream()
                    .map(id -> new Object[]{filmId, id})
                    .toList();
            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    batch
            );
        }
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

    private void assertMpaExists(long mpaId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM MPA_RATINGS WHERE id = ?)", Boolean.class, mpaId);
        if (exists == null || !exists) {
            throw new NotFoundException("MPA with id " + mpaId + " not found");
        }
    }
}