package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

@Repository
@Profile("db")
public class UserDbStorage implements UserStorage {
    @Qualifier("UserDbStorage")
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public User create(User user) {
        String sql = "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE USERS SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (rows == 0) {
            throw new NotFoundException("User with id " + user.getId() + " not found");
        }
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(
                sql,
                this::mapRowToUser,
                id
        );
        return users.stream().findFirst();
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM USERS WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);

        if (rows == 0) {
            throw new NotFoundException("User with id " + id + " not found");
        }
    }

    @Override
    @Transactional
    public User addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Cannot add yourself as a friend");
        }
        assertUserExists(userId);
        assertUserExists(friendId);

        final String sql = "MERGE INTO friendships (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        return findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    @Override
    @Transactional
    public User removeFriend(long userId, long friendId) {
        assertUserExists(userId);
        assertUserExists(friendId);

        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        return findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    @Override
    public Collection<User> findFriends(long userId) {
        assertUserExists(userId);
        final String sql = """
                SELECT u.*
                FROM friendships f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public Collection<User> findMutualFriends(long userId, long otherUserId) {
        assertUserExists(userId);
        assertUserExists(otherUserId);
        final String sql = """
                SELECT DISTINCT u.*
                FROM friendships f1
                JOIN friendships f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON u.id = f1.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherUserId);
    }


    private void assertUserExists(long id) {
        if (findById(id).isEmpty()) {
            throw new NotFoundException("User with id " + id + " not found");
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User u = User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getObject("birthday", LocalDate.class))
                .build();
        u.setFriends(loadFriendIds(u.getId()));
        return u;
    }

    private Set<Long> loadFriendIds(long userId) {
        final String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (r, i) -> r.getLong(1), userId));
    }
}
