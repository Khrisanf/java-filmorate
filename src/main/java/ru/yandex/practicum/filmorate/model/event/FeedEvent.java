package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedEvent {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long entityId;
    private Long timestamp;
}
