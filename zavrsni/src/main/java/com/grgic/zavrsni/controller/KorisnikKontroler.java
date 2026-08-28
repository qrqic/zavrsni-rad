package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.model.Tvrtka;
import com.grgic.zavrsni.model.UlogaKorisnika;
import com.grgic.zavrsni.repository.KorisnikRepository;
import com.grgic.zavrsni.repository.TvrtkaRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class KorisnikKontroler {

    private final KorisnikRepository korisnikRepository;
    private final TvrtkaRepository tvrtkaRepository;
    private final PasswordEncoder passwordEncoder;

    public KorisnikKontroler(KorisnikRepository korisnikRepository, TvrtkaRepository tvrtkaRepository, PasswordEncoder passwordEncoder) {
        this.korisnikRepository = korisnikRepository;
        this.tvrtkaRepository = tvrtkaRepository;
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
    public String registrirajKorisnika(@RequestParam @NotBlank(message = "Ime je obavezno") String ime,
                                        @RequestParam @NotBlank(message = "Prezime je obavezno") String prezime,
                                        @RequestParam @Email(message = "Email adresa nije ispravnog formata") String email,
                                        @RequestParam @Size(min = 6, message = "Lozinka mora imati najmanje 6 znakova") String lozinka,
                                        @RequestParam @NotBlank(message = "Naziv tvrtke je obavezan") String nazivTvrtke,
                                        @RequestParam @Pattern(regexp = "\\d{11}", message = "OIB tvrtke mora sadržavati točno 11 znamenki") String oibTvrtke,
                                        @RequestParam @NotBlank(message = "Mjesto tvrtke je obavezno") String mjestoTvrtke,
                                        @RequestParam @NotBlank(message = "Poštanski broj tvrtke je obavezan") String postanskiBrojTvrtke,
                                        @RequestParam @Pattern(regexp = "HR\\d{19}", message = "IBAN mora biti u hrvatskom formatu (HR + 19 znamenki)") String iban,
                                        @RequestParam(name = "pdvSustav", defaultValue = "false") boolean pdvSustav) {

        Tvrtka tvrtka = new Tvrtka(nazivTvrtke, oibTvrtke, mjestoTvrtke, postanskiBrojTvrtke, iban, pdvSustav);
        tvrtkaRepository.save(tvrtka);

        String hashiranaLozinka = passwordEncoder.encode(lozinka);

        Korisnik noviKorisnik = new Korisnik(tvrtka, email, hashiranaLozinka, ime, prezime, UlogaKorisnika.VLASNIK);
        korisnikRepository.save(noviKorisnik);

        return "redirect:/login";
    }
}
