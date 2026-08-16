package com.grgic.zavrsni.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Klijent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;
    private String email;

    protected Klijent() {
    }

    public Klijent(String naziv, String email) {
        this.naziv = naziv;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getEmail() {
        return email;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
