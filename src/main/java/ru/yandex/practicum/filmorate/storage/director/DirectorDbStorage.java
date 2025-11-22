package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Profile("dbFilms")
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Director> findAll() {
        String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> findById(int id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, this::mapRowToDirector, id);
        return directors.stream().findFirst();
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rows == 0) {
            throw new NotFoundException("Director with id " + director.getId() + " not found");
        }
        return director;
    }

    @Override
    public void deleteById(int id) {
        // Сначала удаляем связи с фильмами
        jdbcTemplate.update("DELETE FROM film_directors WHERE director_id = ?", id);

        // Затем удаляем самого режиссера
        String sql = "DELETE FROM directors WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);

        if (rows == 0) {
            throw new NotFoundException("Director with id " + id + " not found");
        }
    }

    @Override
    public Set<Integer> findMissingIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();

        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Integer> present = jdbcTemplate.queryForList(
                "SELECT id FROM directors WHERE id IN (" + placeholders + ")",
                Integer.class, ids.toArray());

        Set<Integer> missing = new HashSet<>(ids);
        missing.removeAll(present);
        return missing;
    }

    @Override
    public void updateFilmDirectors(long filmId, Set<Director> directors) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        if (directors != null && !directors.isEmpty()) {
            List<Object[]> batch = directors.stream()
                    .filter(Objects::nonNull)
                    .filter(d -> d.getId() != null)
                    .map(d -> new Object[]{filmId, d.getId()})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                    batch
            );
        }
    }

    @Override
    public Set<Director> findDirectorsByFilmId(long filmId) {
        String sql = """
                SELECT d.id, d.name
                FROM film_directors fd
                JOIN directors d ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id
                """;

        return new LinkedHashSet<>(jdbcTemplate.query(sql, this::mapRowToDirector, filmId));
    }

    @Override
    public Collection<Director> findDirectorsByFilmIds(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) return List.of();

        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT d.id, d.name
                FROM film_directors fd
                JOIN directors d ON d.id = fd.director_id
                WHERE fd.film_id IN (%s)
                ORDER BY d.id
                """, placeholders);

        return jdbcTemplate.query(sql, this::mapRowToDirector, filmIds.toArray());
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
