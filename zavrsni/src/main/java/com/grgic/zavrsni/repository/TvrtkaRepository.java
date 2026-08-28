package com.grgic.zavrsni.repository;

import com.grgic.zavrsni.model.Tvrtka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TvrtkaRepository extends JpaRepository<Tvrtka, Long> {

    Optional<Tvrtka> findByOib(String oib);
}
