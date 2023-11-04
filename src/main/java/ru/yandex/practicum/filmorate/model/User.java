package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private int id;
    @Email
    private String email;
    private String login;
    private String name;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthday;
}
