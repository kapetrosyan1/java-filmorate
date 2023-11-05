package ru.yandex.practicum.filmorate.exception;

public class DoesNotExistException extends RuntimeException {
    public DoesNotExistException(String m) {
        super(m);
    }
}