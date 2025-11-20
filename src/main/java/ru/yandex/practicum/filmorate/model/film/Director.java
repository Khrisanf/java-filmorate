package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
public class Director {
    @EqualsAndHashCode.Include
    private Integer id;

    @NotBlank(message = "Director name cannot be blank")
    private String name;
}