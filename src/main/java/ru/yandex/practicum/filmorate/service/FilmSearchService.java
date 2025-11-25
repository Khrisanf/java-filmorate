package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.search.SubstringMatcher;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmSearchService {

    private final FilmService filmService;
    private final SubstringMatcher matcher;

    public FilmSearchService(FilmService filmService, SubstringMatcher matcher) {
        this.filmService = filmService;
        this.matcher = matcher;
    }

    public List<Film> search(String query, Set<SearchBy> searchBy) {
        String normalizedQuery = normalize(query);

        if (normalizedQuery.isEmpty() || searchBy == null || searchBy.isEmpty()) {
            return List.of();
        }

        Collection<Film> allFilms = filmService.findAll();
        Set<Film> result = new LinkedHashSet<>();

        for (Film film : allFilms) {
            if (matchesFilm(film, normalizedQuery, searchBy)) {
                result.add(film);
            }
        }

        return result.stream()
                .sorted(
                        Comparator.comparingInt(this::getPopularity).reversed()
                                .thenComparing(Film::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(Film::getId)
                )
                .peek(f -> log.info("Search result film id={}, name={}, likes={}, date={}",
                        f.getId(), f.getName(), f.getLikes().size(), f.getReleaseDate()))
                .collect(Collectors.toList());
    }

    private boolean matchesFilm(Film film, String normalizedQuery, Set<SearchBy> searchBy) {
        // name search
        if (searchBy.contains(SearchBy.TITLE)) {
            String title = normalize(film.getName());
            if (matcher.contains(title, normalizedQuery)) {
                return true;
            }
        }

        // directors search
        if (searchBy.contains(SearchBy.DIRECTOR)) {
            Set<Director> directors = film.getDirectors();
            if (directors != null) {
                for (Director director : directors) {
                    String directorName = normalize(director.getName());
                    if (matcher.contains(directorName, normalizedQuery)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private int getPopularity(Film film) {
        return film.getLikes().size();
    }
}



