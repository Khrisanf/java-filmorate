package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    private Long userId;
    private Long filmId;
    private String content;
    private Boolean isPositive;
    private int useful;
}

