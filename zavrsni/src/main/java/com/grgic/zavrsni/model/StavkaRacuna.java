package com.grgic.zavrsni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class StavkaRacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "racun_id", nullable = false)
    private Racun racun;

    private String opis;

    @Column(precision = 10, scale = 2)
    private BigDecimal kolicina;

    @Column(precision = 10, scale = 2)
    private BigDecimal jedinicnaCijena;

    @Column(precision = 5, scale = 2)
    private BigDecimal pdvStopa;

    @Column(precision = 12, scale = 2)
    private BigDecimal iznos;

    protected StavkaRacuna() {
    }

    public StavkaRacuna(String opis, BigDecimal kolicina, BigDecimal jedinicnaCijena, BigDecimal pdvStopa) {
        this.opis = opis;
        this.kolicina = kolicina;
        this.jedinicnaCijena = jedinicnaCijena;
        this.pdvStopa = pdvStopa;
        this.iznos = izracunajIznos();
    }

    private BigDecimal izracunajIznos() {
        BigDecimal osnovica = kolicina.multiply(jedinicnaCijena);
        BigDecimal pdvFaktor = BigDecimal.ONE.add(pdvStopa.divide(BigDecimal.valueOf(100)));
        return osnovica.multiply(pdvFaktor).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Racun getRacun() {
        return racun;
    }

    public void setRacun(Racun racun) {
        this.racun = racun;
    }

    public String getOpis() {
        return opis;
    }

    public BigDecimal getKolicina() {
        return kolicina;
    }

    public BigDecimal getJedinicnaCijena() {
        return jedinicnaCijena;
    }

    public BigDecimal getPdvStopa() {
        return pdvStopa;
    }

    public BigDecimal getIznos() {
        return iznos;
    }
}
