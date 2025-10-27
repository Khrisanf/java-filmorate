package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
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
        String sql = "SELECT * FROM USERS";
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
    public User addFriend(long userId, long friendId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public User removeFriend(long userId, long friendId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection<User> findFriends(long userId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public Collection<User> findMutualFriends(long userId, long otherUserId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getObject("birthday", LocalDate.class))
                .build();
    }
}
