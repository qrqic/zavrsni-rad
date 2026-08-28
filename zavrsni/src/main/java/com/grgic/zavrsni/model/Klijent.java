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
public class Klijent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tvrtka_id", nullable = false)
    private Tvrtka tvrtka;

    @Enumerated(EnumType.STRING)
    private TipKlijenta tip;

    private String naziv;
    private String oib;
    private String adresa;
    private String mjesto;
    private String postanskiBroj;
    private String email;
    private String telefon;

    @Column(length = 1000)
    private String napomena;

    private LocalDateTime datumKreiranja;

    protected Klijent() {
    }

    public Klijent(Tvrtka tvrtka, TipKlijenta tip, String naziv, String oib, String adresa, String mjesto,
                   String postanskiBroj, String email, String telefon, String napomena) {
        this.tvrtka = tvrtka;
        this.tip = tip;
        this.naziv = naziv;
        this.oib = oib;
        this.adresa = adresa;
        this.mjesto = mjesto;
        this.postanskiBroj = postanskiBroj;
        this.email = email;
        this.telefon = telefon;
        this.napomena = napomena;
        this.datumKreiranja = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tvrtka getTvrtka() {
        return tvrtka;
    }

    public TipKlijenta getTip() {
        return tip;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getOib() {
        return oib;
    }

    public String getAdresa() {
        return adresa;
    }

    public String getMjesto() {
        return mjesto;
    }

    public String getPostanskiBroj() {
        return postanskiBroj;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getNapomena() {
        return napomena;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setTvrtka(Tvrtka tvrtka) {
        this.tvrtka = tvrtka;
    }

    public void setTip(TipKlijenta tip) {
        this.tip = tip;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public void setOib(String oib) {
        this.oib = oib;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public void setMjesto(String mjesto) {
        this.mjesto = mjesto;
    }

    public void setPostanskiBroj(String postanskiBroj) {
        this.postanskiBroj = postanskiBroj;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
}
