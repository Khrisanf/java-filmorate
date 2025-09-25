package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validation.NotBefore;

import java.time.Duration;
import java.time.LocalDate;


/**
 * Film.
 */
@Getter
@Setter
public class Film {
    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @PastOrPresent
    @NotBefore("1895-12-28")
    private LocalDate releaseDate;

    private Duration duration;
}
