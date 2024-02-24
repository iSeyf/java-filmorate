package ru.yandex.practicum.filmorate.exceptions;

public class ElementNotFoundException extends RuntimeException {
    public ElementNotFoundException(String exception) {
        super(exception);
    }
}
