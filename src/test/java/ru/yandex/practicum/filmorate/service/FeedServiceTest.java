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
                createFeedEvent(1L, userId, EventType.LIKE, EventOperation.ADD, 10L, 1000L),
                createFeedEvent(2L, userId, EventType.FRIEND, EventOperation.ADD, 20L, 2000L)
        );
        when(feedEventStorage.findByUserIdOrderByTimestampAsc(userId)).thenReturn(mockEvents);

        List<FeedEvent> result = feedService.getUserFeed(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        FeedEvent firstEvent = result.get(0);
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

        List<FeedEvent> result = feedService.getUserFeed(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedEventStorage).findByUserIdOrderByTimestampAsc(userId);
    }

    @Test
    void addEvent_shouldSaveEventWithCorrectParameters() {
        Long targetUserId = 1L;
        EventType eventType = EventType.LIKE;
        EventOperation operation = EventOperation.ADD;
        Long entityId = 10L;

        feedService.addEvent(targetUserId, eventType, operation, entityId);

        verify(feedEventStorage).save(argThat(event ->
                event.getUserId().equals(targetUserId) &&
                        event.getEventType() == eventType &&
                        event.getOperation() == operation &&
                        event.getEntityId().equals(entityId) &&
                        event.getTimestamp() > 0
        ));
    }

    @Test
    void addEvent_shouldHandleDifferentEventTypes() {
        Long targetUserId = 1L;

        feedService.addEvent(targetUserId, EventType.FRIEND, EventOperation.ADD, 20L);
        feedService.addEvent(targetUserId, EventType.REVIEW, EventOperation.UPDATE, 30L);
        feedService.addEvent(targetUserId, EventType.LIKE, EventOperation.REMOVE, 40L);

        verify(feedEventStorage, times(3)).save(any(FeedEvent.class));
    }

    @Test
    void getUserFeed_shouldReturnEventsInCorrectOrder() {
        Long userId = 1L;
        when(userStorage.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        List<FeedEvent> mockEvents = Arrays.asList(
                createFeedEvent(1L, userId, EventType.LIKE, EventOperation.ADD, 10L, 1000L),    // самое старое
                createFeedEvent(2L, userId,  EventType.FRIEND, EventOperation.ADD, 20L, 2000L),  // среднее
                createFeedEvent(3L, userId,  EventType.REVIEW, EventOperation.ADD, 30L, 3000L)   // самое новое
        );
        when(feedEventStorage.findByUserIdOrderByTimestampAsc(userId)).thenReturn(mockEvents);

        List<FeedEvent> result = feedService.getUserFeed(userId);

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

        feedService.addEvent(targetUserId, EventType.LIKE, EventOperation.ADD, 10L);

        verify(feedEventStorage).save(argThat(event ->
                event.getTimestamp() >= beforeCall &&
                        event.getTimestamp() <= System.currentTimeMillis()
        ));
    }

    private FeedEvent createFeedEvent(Long eventId, Long userId, EventType eventType,
                                      EventOperation operation, Long entityId, Long timestamp) {
        return FeedEvent.builder()
                .eventId(eventId)
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .timestamp(timestamp)
                .build();
    }
}