package com.grgic.zavrsni.security;

import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.repository.KorisnikRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        return User.builder()
                .username(korisnik.getEmail())
                .password(korisnik.getLozinka())
                .roles(korisnik.getUloga().name())
                .disabled(!korisnik.isAktivan())
                .build();
    }
}
