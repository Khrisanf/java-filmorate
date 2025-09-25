package ru.yandex.practicum.filmorate.exceptions;

public class ValodatorException extends IllegalArgumentException {
    public ValodatorException(String message, Throwable cause) {
        super(message);
    }
}
