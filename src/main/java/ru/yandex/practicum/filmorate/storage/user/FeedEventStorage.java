package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.event.FeedEvent;

import java.util.List;
import java.util.Optional;

public interface FeedEventStorage {
    List<FeedEvent> findByUserIdOrderByTimestampAsc(Long userId);

    FeedEvent save(FeedEvent event);

    Optional<FeedEvent> findById(Long eventId);

    void deleteById(Long eventId);
}
