package com.eevan.bankingservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "Login should not be empty")
    @Column(name = "login")
    private String login;

    @NotEmpty(message = "Password should not be empty")
    @Column(name = "password")
    private String password;

    @NotEmpty(message = "Name should not be empty")
    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 characters")
    @Column(name = "name")
    private String name;

    @NotEmpty(message = "Surname should not be empty")
    @Size(min = 2, max = 30, message = "Surname should be between 2 and 30 characters")
    @Column(name = "surname")
    private String surname;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "birth_date")
    @JsonFormat(pattern = "dd/MM/yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate dateOfBirth;

    @NotEmpty(message = "Phone should not be empty")
    @Column(name = "phone_main", unique = true)
    private String phoneNumberMain;

    @Column(name = "phone_additional", unique = true)
    private String phoneNumberAdditional;

    @NotEmpty(message = "Email should not be empty")
    @Column(name = "email_main", unique = true)
    private String emailMain;

    @Column(name = "email_additional", unique = true)
    private String emailAdditional;

    @Column(name = "funds")
    private BigDecimal funds;

    public Client(String name, String surname, String patronymic, LocalDate dateOfBirth, String phoneNumberMain, String phoneNumberAdditional, String emailMain, String emailAdditional, String login, BigDecimal funds) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumberMain = phoneNumberMain;
        this.phoneNumberAdditional = phoneNumberAdditional;
        this.emailMain = emailMain;
        this.emailAdditional = emailAdditional;
        this.login = login;
        this.funds = funds;
    }

}
