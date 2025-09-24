package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
    @Size(min = 6, max = 20)
    private String login;
    @Size(max = 10)
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
