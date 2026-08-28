package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Klijent;
import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.model.TipKlijenta;
import com.grgic.zavrsni.model.Tvrtka;
import com.grgic.zavrsni.repository.KlijentRepository;
import com.grgic.zavrsni.repository.KorisnikRepository;
import com.grgic.zavrsni.repository.RacunRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@Validated
public class KlijentKontroler {

    private final KlijentRepository klijentRepository;
    private final KorisnikRepository korisnikRepository;
    private final RacunRepository racunRepository;

    public KlijentKontroler(KlijentRepository klijentRepository, KorisnikRepository korisnikRepository,
                             RacunRepository racunRepository) {
        this.klijentRepository = klijentRepository;
        this.korisnikRepository = korisnikRepository;
        this.racunRepository = racunRepository;
    }

    private Tvrtka dohvatiTvrtkuPrijavljenogKorisnika(Authentication authentication) {
        String email = authentication.getName();
        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Prijavljeni korisnik s emailom " + email + " ne postoji"));
        return korisnik.getTvrtka();
    }

    @GetMapping("/klijenti")
    public String prikaziKlijente(Model model, Authentication authentication,
                                   @RequestParam(required = false) String pretraga,
                                   @RequestParam(required = false) String datumOd,
                                   @RequestParam(required = false) String datumDo,
                                   @RequestParam(required = false) String sortiranje) {

        popuniModelPretrazenimKlijentima(model, authentication, pretraga, datumOd, datumDo, sortiranje);

        return "klijenti";
    }

    @GetMapping("/klijenti/rezultati")
    public String prikaziRezultatePretrage(Model model, Authentication authentication,
                                            @RequestParam(required = false) String pretraga,
                                            @RequestParam(required = false) String datumOd,
                                            @RequestParam(required = false) String datumDo,
                                            @RequestParam(required = false) String sortiranje) {

        popuniModelPretrazenimKlijentima(model, authentication, pretraga, datumOd, datumDo, sortiranje);

        return "klijenti :: rezultati";
    }

    private void popuniModelPretrazenimKlijentima(Model model, Authentication authentication, String pretraga,
                                                    String datumOd, String datumDo, String sortiranje) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);

        String pojam = pretraga != null ? pretraga.trim() : "";
        LocalDateTime pocetakRazdoblja = parsirajDatum(datumOd, LocalTime.MIN);
        LocalDateTime krajRazdoblja = parsirajDatum(datumDo, LocalTime.MAX);
        String odabranoSortiranje = sortiranje != null ? sortiranje : "naziv_asc";

        List<Klijent> klijenti = klijentRepository.pretraziKlijente(
                tvrtka, pojam, pocetakRazdoblja, krajRazdoblja, izradiSort(odabranoSortiranje));

        model.addAttribute("klijenti", klijenti);
        model.addAttribute("pretraga", pretraga);
        model.addAttribute("datumOd", datumOd);
        model.addAttribute("datumDo", datumDo);
        model.addAttribute("sortiranje", odabranoSortiranje);
    }

    private Sort izradiSort(String sortiranje) {
        return switch (sortiranje) {
            case "naziv_desc" -> Sort.by("naziv").descending();
            case "datum_desc" -> Sort.by("datumKreiranja").descending();
            case "datum_asc" -> Sort.by("datumKreiranja").ascending();
            default -> Sort.by("naziv").ascending();
        };
    }

    private LocalDateTime parsirajDatum(String datum, LocalTime vrijeme) {
        if (datum == null || datum.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(datum).atTime(vrijeme);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @GetMapping("/klijenti/novi")
    public String prikaziFormu() {
        return "novi-klijent";
    }

    @PostMapping("/klijenti")
    public String dodajKlijenta(@RequestParam TipKlijenta tip,
                                 @RequestParam @NotBlank(message = "Naziv je obavezan") String naziv,
                                 @RequestParam @Pattern(regexp = "\\d{11}", message = "OIB mora sadržavati točno 11 znamenki") String oib,
                                 @RequestParam @NotBlank(message = "Adresa je obavezna") String adresa,
                                 @RequestParam @NotBlank(message = "Mjesto je obavezno") String mjesto,
                                 @RequestParam @NotBlank(message = "Poštanski broj je obavezan") String postanskiBroj,
                                 @RequestParam @Email(message = "Email adresa nije ispravnog formata") String email,
                                 @RequestParam @NotBlank(message = "Telefon je obavezan") String telefon,
                                 @RequestParam(required = false) String napomena,
                                 Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent noviKlijent = new Klijent(tvrtka, tip, naziv, oib, adresa, mjesto, postanskiBroj, email, telefon, napomena);

        klijentRepository.save(noviKlijent);

        return "redirect:/klijenti";
    }


    @GetMapping("/klijenti/{id}")
    public String prikaziPregled(@PathVariable Long id, Model model, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent klijent = klijentRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        model.addAttribute("klijent", klijent);
        model.addAttribute("racuni", racunRepository.findByKlijentOrderByDatumIzdavanjaDesc(klijent));

        return "pregled-klijent";
    }

    @GetMapping("/klijenti/{id}/uredi")
    public String prikaziFormuZaUredivanje(@PathVariable Long id, Model model, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent klijent = klijentRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        model.addAttribute("klijent", klijent);

        return "uredi-klijent";
    }

    @PostMapping("/klijenti/{id}")
    public String azurirajKlijenta(@PathVariable Long id,
                                    @RequestParam TipKlijenta tip,
                                    @RequestParam @NotBlank(message = "Naziv je obavezan") String naziv,
                                    @RequestParam @Pattern(regexp = "\\d{11}", message = "OIB mora sadržavati točno 11 znamenki") String oib,
                                    @RequestParam @NotBlank(message = "Adresa je obavezna") String adresa,
                                    @RequestParam @NotBlank(message = "Mjesto je obavezno") String mjesto,
                                    @RequestParam @NotBlank(message = "Poštanski broj je obavezan") String postanskiBroj,
                                    @RequestParam @Email(message = "Email adresa nije ispravnog formata") String email,
                                    @RequestParam @NotBlank(message = "Telefon je obavezan") String telefon,
                                    @RequestParam(required = false) String napomena,
                                    Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent klijent = klijentRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        klijent.setTip(tip);
        klijent.setNaziv(naziv);
        klijent.setOib(oib);
        klijent.setAdresa(adresa);
        klijent.setMjesto(mjesto);
        klijent.setPostanskiBroj(postanskiBroj);
        klijent.setEmail(email);
        klijent.setTelefon(telefon);
        klijent.setNapomena(napomena);

        klijentRepository.save(klijent);

        return "redirect:/klijenti/" + id;
    }


    @PostMapping("/klijenti/{id}/obrisi")
    public String obrisiKlijenta(@PathVariable Long id, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent klijent = klijentRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        klijentRepository.delete(klijent);

        return "redirect:/klijenti";
    }
}
