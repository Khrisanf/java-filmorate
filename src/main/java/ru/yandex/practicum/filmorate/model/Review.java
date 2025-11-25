package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    @NotNull(message = "userId не может быть пустым")
    private Long userId;
    @NotNull(message = "filmId не может быть пустым")
    private Long filmId;
    private String content;
    private Boolean isPositive;
    private int useful;
}

