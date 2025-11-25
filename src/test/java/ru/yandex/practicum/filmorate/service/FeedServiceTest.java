package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.FeedEvent;
import ru.yandex.practicum.filmorate.model.event.FeedEventResponse;
import ru.yandex.practicum.filmorate.storage.user.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedEventStorage feedEventStorage;

    @Mock
    private UserStorage userStorage;

    @Spy
    @InjectMocks
    private FeedService feedService;

    @Test
    void getUserFeed_shouldReturnEventsWhenUserExists() {
        Long userId = 1L;
        when(userStorage.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        List<FeedEvent> mockEvents = Arrays.asList(
                createFeedEvent(1L, userId, 1L, EventType.LIKE, EventOperation.ADD, 10L, 1000L),
                createFeedEvent(2L, userId, 1L, EventType.FRIEND, EventOperation.ADD, 20L, 2000L)
        );
        when(feedEventStorage.findByUserIdOrderByTimestampAsc(userId)).thenReturn(mockEvents);

        List<FeedEventResponse> result = feedService.getUserFeed(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        FeedEventResponse firstEvent = result.get(0);
        assertEquals(1L, firstEvent.getEventId());
        assertEquals(1L, firstEvent.getUserId());
        assertEquals(EventType.LIKE, firstEvent.getEventType());
        assertEquals(EventOperation.ADD, firstEvent.getOperation());
        assertEquals(10L, firstEvent.getEntityId());
        assertEquals(1000L, firstEvent.getTimestamp());

        verify(feedEventStorage).findByUserIdOrderByTimestampAsc(userId);
    }

    @Test
    void getUserFeed_shouldReturnEmptyListWhenNoEvents() {
        Long userId = 1L;
        when(userStorage.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(feedEventStorage.findByUserIdOrderByTimestampAsc(userId)).thenReturn(Collections.emptyList());

        List<FeedEventResponse> result = feedService.getUserFeed(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedEventStorage).findByUserIdOrderByTimestampAsc(userId);
    }

    @Test
    void addEvent_shouldSaveEventWithCorrectParameters() {
        Long targetUserId = 1L;
        Long actorId = 2L;
        EventType eventType = EventType.LIKE;
        EventOperation operation = EventOperation.ADD;
        Long entityId = 10L;
        String entityType = "FILM";

        feedService.addEvent(targetUserId, actorId, eventType, operation, entityId, entityType);

        verify(feedEventStorage).save(argThat(event ->
                event.getUserId().equals(targetUserId) &&
                        event.getActorId().equals(actorId) &&
                        event.getEventType() == eventType &&
                        event.getOperation() == operation &&
                        event.getEntityId().equals(entityId) &&
                        event.getEntityType().equals(entityType) &&
                        event.getTimestamp() > 0
        ));
    }

    @Test
    void addEvent_shouldHandleDifferentEventTypes() {
        Long targetUserId = 1L;
        Long actorId = 1L;

        feedService.addEvent(targetUserId, actorId, EventType.FRIEND, EventOperation.ADD, 20L, "USER");
        feedService.addEvent(targetUserId, actorId, EventType.REVIEW, EventOperation.UPDATE, 30L, "REVIEW");
        feedService.addEvent(targetUserId, actorId, EventType.LIKE, EventOperation.REMOVE, 40L, "FILM");

        verify(feedEventStorage, times(3)).save(any(FeedEvent.class));
    }

    @Test
    void getUserFeed_shouldReturnEventsInCorrectOrder() {
        Long userId = 1L;
        when(userStorage.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        List<FeedEvent> mockEvents = Arrays.asList(
                createFeedEvent(1L, userId, 1L, EventType.LIKE, EventOperation.ADD, 10L, 1000L),    // самое старое
                createFeedEvent(2L, userId, 1L, EventType.FRIEND, EventOperation.ADD, 20L, 2000L),  // среднее
                createFeedEvent(3L, userId, 1L, EventType.REVIEW, EventOperation.ADD, 30L, 3000L)   // самое новое
        );
        when(feedEventStorage.findByUserIdOrderByTimestampAsc(userId)).thenReturn(mockEvents);

        List<FeedEventResponse> result = feedService.getUserFeed(userId);

        assertEquals(3, result.size());
        assertEquals(1000L, result.get(0).getTimestamp());
        assertEquals(2000L, result.get(1).getTimestamp());
        assertEquals(3000L, result.get(2).getTimestamp());
    }

    @Test
    void addEvent_shouldSetCurrentTimestamp() {
        Long targetUserId = 1L;
        Long actorId = 1L;

        long beforeCall = System.currentTimeMillis();

        feedService.addEvent(targetUserId, actorId, EventType.LIKE, EventOperation.ADD, 10L, "FILM");

        verify(feedEventStorage).save(argThat(event ->
                event.getTimestamp() >= beforeCall &&
                        event.getTimestamp() <= System.currentTimeMillis()
        ));
    }

    private FeedEvent createFeedEvent(Long eventId, Long userId, Long actorId, EventType eventType,
                                      EventOperation operation, Long entityId, Long timestamp) {
        return FeedEvent.builder()
                .eventId(eventId)
                .userId(userId)
                .actorId(actorId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .entityType("FILM")
                .timestamp(timestamp)
                .build();
    }
}