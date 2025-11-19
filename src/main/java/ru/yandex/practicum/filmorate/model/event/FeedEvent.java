package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedEvent {
    private Long eventId;
    private Long userId;
    private Long actorId;
    private EventType eventType;
    private EventOperation operation;
    private Long entityId;
    private String entityType;
    private Long timestamp;
}
