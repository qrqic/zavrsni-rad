package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.repository.KorisnikRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class KorisnikKontroler {

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    public KorisnikKontroler(KorisnikRepository korisnikRepository, PasswordEncoder passwordEncoder) {
        this.korisnikRepository = korisnikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registracija")
    public String prikaziFormu() {
        return "registracija";
    }

    @GetMapping("/login")
    public String prikaziLoginFormu() {
        return "login";
    }

    @PostMapping("/registracija")
    public String registrirajKorisnika(@RequestParam String ime,
                                        @RequestParam String prezime,
                                        @RequestParam String email,
                                        @RequestParam String lozinka) {

        String hashiranaLozinka = passwordEncoder.encode(lozinka);

        Korisnik noviKorisnik = new Korisnik(ime, prezime, email, hashiranaLozinka);
        korisnikRepository.save(noviKorisnik);

        return "redirect:/login";
    }
}
