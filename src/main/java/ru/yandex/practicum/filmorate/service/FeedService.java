package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.FeedEvent;
import ru.yandex.practicum.filmorate.model.event.FeedEventResponse;
import ru.yandex.practicum.filmorate.storage.user.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedEventStorage feedEventStorage;
    private final UserStorage userStorage;

    public List<FeedEventResponse> getUserFeed(Long userId) {
        List<FeedEvent> events = feedEventStorage.findByUserIdOrderByTimestampAsc(userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " нет"));

        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void addEvent(Long targetUserId, Long actorId, EventType eventType,
                         EventOperation operation, Long entityId, String entityType) {

        FeedEvent event = FeedEvent.builder()
                .userId(targetUserId)
                .actorId(actorId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .entityType(entityType)
                .timestamp(System.currentTimeMillis())
                .build();

        feedEventStorage.save(event);
    }

    private FeedEventResponse convertToDto(FeedEvent event) {
        return FeedEventResponse.builder()
                .eventId(event.getEventId())
                .userId(event.getActorId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .entityId(event.getEntityId())
                .timestamp(event.getTimestamp())
                .build();
    }
}
