package com.grgic.zavrsni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Tvrtka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;

    @Column(unique = true, nullable = false)
    private String oib;

    private String mjesto;
    private String postanskiBroj;
    private String iban;

    private boolean pdvSustav;

    private LocalDateTime datumKreiranja;

    protected Tvrtka() {
    }

    public Tvrtka(String naziv, String oib, String mjesto, String postanskiBroj, String iban, boolean pdvSustav) {
        this.naziv = naziv;
        this.oib = oib;
        this.mjesto = mjesto;
        this.postanskiBroj = postanskiBroj;
        this.iban = iban;
        this.pdvSustav = pdvSustav;
        this.datumKreiranja = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getOib() {
        return oib;
    }

    public String getMjesto() {
        return mjesto;
    }

    public String getPostanskiBroj() {
        return postanskiBroj;
    }

    public String getIban() {
        return iban;
    }

    public boolean isPdvSustav() {
        return pdvSustav;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public void setOib(String oib) {
        this.oib = oib;
    }

    public void setMjesto(String mjesto) {
        this.mjesto = mjesto;
    }

    public void setPostanskiBroj(String postanskiBroj) {
        this.postanskiBroj = postanskiBroj;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setPdvSustav(boolean pdvSustav) {
        this.pdvSustav = pdvSustav;
    }
}
