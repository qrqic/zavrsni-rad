package com.grgic.zavrsni.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tvrtka_id", "broj_racuna"}))
public class Racun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tvrtka_id", nullable = false)
    private Tvrtka tvrtka;

    @ManyToOne
    @JoinColumn(name = "klijent_id", nullable = false)
    private Klijent klijent;

    @Column(name = "broj_racuna", nullable = false)
    private String brojRacuna;

    private LocalDate datumIzdavanja;
    private LocalDate datumDospijeca;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private StatusRacuna status;

    @Column(length = 1000)
    private String napomena;

    @Column(precision = 12, scale = 2)
    private BigDecimal ukupanIznos;

    private LocalDateTime datumKreiranja;

    @OneToMany(mappedBy = "racun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StavkaRacuna> stavke = new ArrayList<>();

    protected Racun() {
    }

    public Racun(Tvrtka tvrtka, Klijent klijent, String brojRacuna, LocalDate datumIzdavanja,
                 LocalDate datumDospijeca, String napomena) {
        this.tvrtka = tvrtka;
        this.klijent = klijent;
        this.brojRacuna = brojRacuna;
        this.datumIzdavanja = datumIzdavanja;
        this.datumDospijeca = datumDospijeca;
        this.napomena = napomena;
        this.status = StatusRacuna.NEPLACEN;
        this.ukupanIznos = BigDecimal.ZERO;
        this.datumKreiranja = LocalDateTime.now();
    }

    public void dodajStavku(StavkaRacuna stavka) {
        stavka.setRacun(this);
        stavke.add(stavka);
        izracunajUkupanIznos();
    }

    private void izracunajUkupanIznos() {
        this.ukupanIznos = stavke.stream()
                .map(StavkaRacuna::getIznos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Tvrtka getTvrtka() {
        return tvrtka;
    }

    public Klijent getKlijent() {
        return klijent;
    }

    public String getBrojRacuna() {
        return brojRacuna;
    }

    public LocalDate getDatumIzdavanja() {
        return datumIzdavanja;
    }

    public LocalDate getDatumDospijeca() {
        return datumDospijeca;
    }

    public StatusRacuna getStatus() {
        return status;
    }

    public void setStatus(StatusRacuna status) {
        this.status = status;
    }

    public String getNapomena() {
        return napomena;
    }

    public BigDecimal getUkupanIznos() {
        return ukupanIznos;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public List<StavkaRacuna> getStavke() {
        return stavke;
    }
}
