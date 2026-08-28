package com.grgic.zavrsni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tvrtka_id", nullable = false)
    private Tvrtka tvrtka;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String lozinka;

    private String ime;
    private String prezime;

    @Enumerated(EnumType.STRING)
    private UlogaKorisnika uloga;

    private boolean aktivan;

    private LocalDateTime datumKreiranja;

    protected Korisnik() {
    }

    public Korisnik(Tvrtka tvrtka, String email, String lozinka, String ime, String prezime, UlogaKorisnika uloga) {
        this.tvrtka = tvrtka;
        this.email = email;
        this.lozinka = lozinka;
        this.ime = ime;
        this.prezime = prezime;
        this.uloga = uloga;
        this.aktivan = true;
        this.datumKreiranja = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tvrtka getTvrtka() {
        return tvrtka;
    }

    public String getEmail() {
        return email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public UlogaKorisnika getUloga() {
        return uloga;
    }

    public boolean isAktivan() {
        return aktivan;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setTvrtka(Tvrtka tvrtka) {
        this.tvrtka = tvrtka;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public void setUloga(UlogaKorisnika uloga) {
        this.uloga = uloga;
    }

    public void setAktivan(boolean aktivan) {
        this.aktivan = aktivan;
    }
}
