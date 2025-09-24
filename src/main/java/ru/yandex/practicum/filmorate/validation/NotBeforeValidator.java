package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class NotBeforeValidator implements ConstraintValidator<NotBefore, LocalDate> {

    private LocalDate startDate;

    @Override
    public void initialize(NotBefore constraintAnnotation) {
        try {
            this.startDate = LocalDate.parse(constraintAnnotation.value());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid @NotBefore value: " + constraintAnnotation.value() +
                            ". Use ISO format YYYY-MM-DD.", e
            );
        }
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(startDate);
    }
}
