package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^\\S+$", message = "login must not contain spaces")
    private String login;
    @Size(max = 10)
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
