package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.FeedEvent;
import ru.yandex.practicum.filmorate.storage.user.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedEventStorage feedEventStorage;
    private final UserStorage userStorage;

    public List<FeedEvent> getUserFeed(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        return feedEventStorage.findByUserIdOrderByTimestampAsc(userId);
    }

    public void addEvent(Long targetUserId, EventType eventType,
                         EventOperation operation, Long entityId) {
        log.debug("Adding feed event for user {}: {} {}", targetUserId, eventType, operation);

        FeedEvent event = FeedEvent.builder()
                .userId(targetUserId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .timestamp(System.currentTimeMillis())
                .build();

        feedEventStorage.save(event);
    }
}
