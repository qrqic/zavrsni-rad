package com.grgic.zavrsni.repository;

import com.grgic.zavrsni.model.Klijent;
import com.grgic.zavrsni.model.Racun;
import com.grgic.zavrsni.model.StatusRacuna;
import com.grgic.zavrsni.model.Tvrtka;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RacunRepository extends JpaRepository<Racun, Long> {

    List<Racun> findByTvrtka(Tvrtka tvrtka);

    List<Racun> findByKlijentOrderByDatumIzdavanjaDesc(Klijent klijent);

    Optional<Racun> findByIdAndTvrtka(Long id, Tvrtka tvrtka);

    long countByTvrtkaAndDatumIzdavanjaBetween(
            Tvrtka tvrtka, LocalDate pocetakGodine, LocalDate krajGodine);

    @Query("SELECT r FROM Racun r WHERE r.tvrtka = :tvrtka " +
            "AND (:klijentId IS NULL OR r.klijent.id = :klijentId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:datumOd IS NULL OR r.datumIzdavanja >= :datumOd) " +
            "AND (:datumDo IS NULL OR r.datumIzdavanja <= :datumDo)")
    List<Racun> pretraziRacune(@Param("tvrtka") Tvrtka tvrtka,
                                @Param("klijentId") Long klijentId,
                                @Param("status") StatusRacuna status,
                                @Param("datumOd") LocalDate datumOd,
                                @Param("datumDo") LocalDate datumDo,
                                Sort sortiranje);
}
