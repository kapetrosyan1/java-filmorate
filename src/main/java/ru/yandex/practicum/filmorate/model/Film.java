package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    private int id;
    private String name;
    private String description;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate releaseDate;
    private long duration;
}