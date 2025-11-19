package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedEventDto {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long eventId;
    private Long entityId;
}
