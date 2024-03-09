package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private HashMap<Integer, Film> films = new HashMap<>();
    private int filmId;

    @Override
    public List<Film> getFilms() {
        log.info("Вызван GET-запрос. Получен список фильмов.");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        if (checkValid(film)) {
            film.setId(getNewId());
            films.put(film.getId(), film);
            log.info("Добавлен фильм: {}", film.getName());
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ElementNotFoundException("Объект не найден.");
        }
        Set<Integer> likes = films.get(film.getId()).getLikes();
        film.setLikes(likes);
        if (checkValid(film)) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен.", film.getName());
        }
        return film;
    }

    @Override
    public Film getFilm(int id) {
        if (!films.containsKey(id)) {
            throw new ElementNotFoundException("Фильм " + id + " не найден.");
        }
        return films.get(id);
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
