package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.CommonFilmsService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class CommonFilmsController {

    private final CommonFilmsService commonFilmsService;

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long friendId) {

        Collection<Film> commonFilms = commonFilmsService.getCommonFilms(userId, friendId);
        return ResponseEntity.ok(commonFilms);
    }
}
