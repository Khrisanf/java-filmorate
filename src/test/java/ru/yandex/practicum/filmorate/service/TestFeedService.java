package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

public class TestFeedService extends FeedService {
    public TestFeedService() {
        super(null);
    }

    @Override
    public void addEvent(Long targetUserId, Long actorId, EventType eventType,
                         EventOperation operation, Long entityId, String entityType) {
        System.out.println("Feed event skipped for testing: " + eventType);
    }
}
