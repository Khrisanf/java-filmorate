package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewLike {
    private Long reviewId;
    private Long userId;
    private Boolean isLike;
}
