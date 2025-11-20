package ru.yandex.practicum.filmorate.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("error")
        String title,

        int status,

        @JsonProperty("description")
        String detail,

        String path
) {
}