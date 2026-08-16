package com.grgic.zavrsni.repository;

import com.grgic.zavrsni.model.Klijent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KlijentRepository extends JpaRepository<Klijent, Long> {
}
