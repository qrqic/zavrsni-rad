package com.grgic.zavrsni.repository;

import com.grgic.zavrsni.model.Klijent;
import com.grgic.zavrsni.model.Tvrtka;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KlijentRepository extends JpaRepository<Klijent, Long> {

    List<Klijent> findByTvrtka(Tvrtka tvrtka);

    Optional<Klijent> findByIdAndTvrtka(Long id, Tvrtka tvrtka);

    @Query("SELECT k FROM Klijent k WHERE k.tvrtka = :tvrtka " +
            "AND (LOWER(k.naziv) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.oib) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.adresa) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.mjesto) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.postanskiBroj) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.email) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.telefon) LIKE LOWER(CONCAT('%', :pojam, '%')) " +
            "  OR LOWER(k.napomena) LIKE LOWER(CONCAT('%', :pojam, '%'))) " +
            "AND (:datumOd IS NULL OR k.datumKreiranja >= :datumOd) " +
            "AND (:datumDo IS NULL OR k.datumKreiranja <= :datumDo)")
    List<Klijent> pretraziKlijente(@Param("tvrtka") Tvrtka tvrtka,
                                    @Param("pojam") String pojam,
                                    @Param("datumOd") LocalDateTime datumOd,
                                    @Param("datumDo") LocalDateTime datumDo,
                                    Sort sortiranje);
}
