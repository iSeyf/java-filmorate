package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequestMapping("/films")
@RestController
public class FilmController {
    private static final LocalDate RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private HashMap<Integer, Film> films = new HashMap<>();
    private int filmId;

    @GetMapping
    public List<Film> getFilms() {
        log.info("Вызван GET-запрос. Получен список фильмов.");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        if (checkValid(film)) {
            film.setId(getNewId());
            films.put(film.getId(), film);
            log.info("Добавлен фильм: {}", film.getName());
        }
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Объект не найден.");
        }
        if (checkValid(film)) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен.", film.getName());
        }
        return film;
    }

    private boolean checkValid(Film film) {
        if (film.getReleaseDate().isBefore(RELEASE_DATE)) {
            log.info("Введена некоректная дата релиза.");
            throw new ValidationException("Фильм не мог выйти раньше 28.12.1895.");
        }
        return true;
    }

    private int getNewId() {
        return ++filmId;
    }
}
