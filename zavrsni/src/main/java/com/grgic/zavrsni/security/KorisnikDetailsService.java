package com.grgic.zavrsni.security;

import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.repository.KorisnikRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// UserDetailsService je "prevoditelj" izmedu naseg Korisnik entiteta i onoga
// sto Spring Security zna citati (UserDetails). Kad se netko pokusa prijaviti,
// Spring Security sam pozove loadUserByUsername(email) i usporedi lozinku.
@Service
public class KorisnikDetailsService implements UserDetailsService {

    private final KorisnikRepository korisnikRepository;

    public KorisnikDetailsService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik s emailom " + email + " ne postoji"));

        // User.builder() gradi Spring Securityjev standardni UserDetails objekt.
        // .password() ocekuje vec HASHIRANU lozinku (onu koju smo spremili u bazu) -
        // Spring Security sam usporeduje uneseni tekst s hashem, mi to ne radimo rucno.
        // .roles("USER") je obavezno bar jedna "uloga" da autentifikacija prode.
        return User.builder()
                .username(korisnik.getEmail())
                .password(korisnik.getLozinka())
                .roles("USER")
                .build();
    }
}
