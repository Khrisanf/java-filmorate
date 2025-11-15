package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmSearchService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class SearchController {
    private final FilmSearchService filmSearchService;

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam String by
    ) {
        Set<SearchBy> searchBy = parseSearchBy(by);
        return filmSearchService.search(query, searchBy);
    }

    private Set<SearchBy> parseSearchBy(String by) {
        return Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(SearchBy::valueOf)
                .collect(Collectors.toSet());
    }
}
