package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.FeedEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class FeedEventDbStorage implements FeedEventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FeedEventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FeedEvent> findByUserIdOrderByTimestampAsc(Long userId) {
        String sql = "SELECT * FROM user_feed_events WHERE user_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRow, userId);
    }

    @Override
    public FeedEvent save(FeedEvent event) {
        if (event.getEventId() == null) {
            return insert(event);
        } else {
            return update(event);
        }
    }

    @Override
    public Optional<FeedEvent> findById(Long eventId) {
        String sql = "SELECT * FROM user_feed_events WHERE event_id = ?";
        List<FeedEvent> result = jdbcTemplate.query(sql, this::mapRow, eventId);
        return result.stream().findFirst();
    }

    @Override
    public void deleteById(Long eventId) {
        String sql = "DELETE FROM user_feed_events WHERE event_id = ?";
        jdbcTemplate.update(sql, eventId);
    }

    private FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FeedEvent.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .actorId(rs.getLong("actor_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(EventOperation.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .entityType(rs.getString("entity_type"))
                .timestamp(rs.getLong("timestamp"))
                .build();
    }

    private FeedEvent insert(FeedEvent event) {
        String sql = "INSERT INTO user_feed_events (user_id, actor_id, event_type, operation, entity_id, entity_type, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
            ps.setLong(1, event.getUserId());
            ps.setLong(2, event.getActorId());
            ps.setString(3, event.getEventType().name());
            ps.setString(4, event.getOperation().name());
            ps.setLong(5, event.getEntityId());
            ps.setString(6, event.getEntityType());
            ps.setLong(7, event.getTimestamp());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            event.setEventId(keyHolder.getKey().longValue());
        }
        return event;
    }

    private FeedEvent update(FeedEvent event) {
        String sql = "UPDATE user_feed_events SET user_id = ?, actor_id = ?, event_type = ?, operation = ?, " +
                "entity_id = ?, entity_type = ?, timestamp = ? WHERE event_id = ?";

        jdbcTemplate.update(sql,
                event.getUserId(),
                event.getActorId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId(),
                event.getEntityType(),
                event.getTimestamp(),
                event.getEventId()
        );
        return event;
    }
}
