package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public void addLike(int id, int userId) {
        userStorage.getUser(userId);
        getFilm(id).addLike(userId);
    }

    public void deleteLike(int id, int userId) {
        userStorage.getUser(userId);
        if (!getFilm(id).getLikes().contains(userId)) {
            throw new ElementNotFoundException("Лайк не найден.");
        }
        getFilm(id).deleteLike(userId);
    }

    public List<Film> getTopFilms(int count) {
        List<Film> topFilms = getFilms().stream()
                .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());

        return topFilms;
    }
}
