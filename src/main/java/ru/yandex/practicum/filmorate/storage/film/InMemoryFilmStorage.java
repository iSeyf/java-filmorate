package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private HashMap<Integer, Film> films = new HashMap<>();
    private HashMap<Integer, List<Integer>> filmLikes = new HashMap<>();
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
            filmLikes.put(film.getId(), new ArrayList<>());
            log.info("Добавлен фильм: {}", film.getName());
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ElementNotFoundException("Объект не найден.");
        }
        int likes = films.get(film.getId()).getLikesCount();
        film.setLikesCount(likes);
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

    @Override
    public void addLike(Integer id, Integer userId) {
        getFilm(id);
        filmLikes.get(id).add(userId);
        getFilm(id).setLikesCount(getFilm(id).getLikesCount() + 1);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        getFilm(id);
        filmLikes.get(id).remove(userId);
        getFilm(id).setLikesCount(getFilm(id).getLikesCount() - 1);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return getFilms().stream()
                .sorted(Comparator.comparingInt(film -> -film.getLikesCount()))
                .limit(count)
                .collect(Collectors.toList());
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
