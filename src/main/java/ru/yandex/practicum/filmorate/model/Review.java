package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    @NotNull(message = "userId cannot be empty")
    private Long userId;
    @NotNull(message = "filmID cannot be empty")
    private Long filmId;
    private String content;
    private Boolean isPositive;
    private int useful;
}

