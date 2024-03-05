package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public HashMap<Integer, Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        if (!getFilms().containsKey(id)) {
            throw new ElementNotFoundException("Фильм " + id + " не найден.");
        }
        return getFilms().get(id);
    }

    public void addLike(int id, int userId) {
        if (!getFilms().containsKey(id)) {
            throw new ElementNotFoundException("Фильм " + id + " не найден.");
        }
        getFilm(id).addLike(userId);
    }

    public void deleteLike(int id, int userId) {
        if (!getFilm(id).getLikes().contains(userId)) {
            throw new ElementNotFoundException("Лайк не найден.");
        }
        getFilm(id).deleteLike(userId);
    }

    public List<Film> getTopFilms(int count) {
        Collection<Film> films = getFilms().values();

        List<Film> topFilms = films.stream()
                .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());

        return topFilms;
    }
}
