package com.grgic.zavrsni.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

// Hvata sve neuhvaćene iznimke iz kontrolera i umjesto zadane Whitelabel error stranice
// prikazuje korisniku razumljivu poruku na stranici greska.html.
@ControllerAdvice
public class IznimkeKontroler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String obradiNepostojeciZapis(IllegalArgumentException e, Model model) {
        model.addAttribute("poruka", e.getMessage());
        return "greska";
    }

    @ExceptionHandler(NevazeciPodaciException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String obradiNevazecePodatke(NevazeciPodaciException e, Model model) {
        model.addAttribute("poruka", e.getMessage());
        return "greska";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String obradiPogresneParametre(ConstraintViolationException e, Model model) {
        String poruka = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        model.addAttribute("poruka", "Neispravni podaci: " + poruka);
        return "greska";
    }

    // Spring MVC od verzije 6.1 validaciju @RequestParam vrijednosti na kontroleru prijavljuje
    // kroz ovu iznimku umjesto kroz jakarta.validation.ConstraintViolationException.
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String obradiPogresneParametreMetode(HandlerMethodValidationException e, Model model) {
        String poruka = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        model.addAttribute("poruka", "Neispravni podaci: " + poruka);
        return "greska";
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String obradiNevazecoStanje(IllegalStateException e, Model model) {
        model.addAttribute("poruka", e.getMessage());
        return "greska";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String obradiOstaleGreske(Exception e, Model model) {
        model.addAttribute("poruka", "Došlo je do neočekivane greške. Pokušajte ponovno.");
        return "greska";
    }
}
