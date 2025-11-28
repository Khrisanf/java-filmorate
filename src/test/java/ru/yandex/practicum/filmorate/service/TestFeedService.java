package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

public class TestFeedService extends FeedService {
    public TestFeedService() {
        super(null, null);
    }

    @Override
    public void addEvent(Long targetUserId, EventType eventType,
                         EventOperation operation, Long entityId) {
        System.out.println("Feed event skipped for testing: " + eventType);
    }
}
