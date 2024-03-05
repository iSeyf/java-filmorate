package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;

public interface FilmStorage {
    HashMap<Integer, Film> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);
}
