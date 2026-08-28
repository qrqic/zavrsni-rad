package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Klijent;
import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.model.Racun;
import com.grgic.zavrsni.model.StatusRacuna;
import com.grgic.zavrsni.model.StavkaRacuna;
import com.grgic.zavrsni.model.Tvrtka;
import com.grgic.zavrsni.repository.KlijentRepository;
import com.grgic.zavrsni.repository.KorisnikRepository;
import com.grgic.zavrsni.repository.RacunRepository;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@Validated
public class RacunKontroler {

    private final RacunRepository racunRepository;
    private final KlijentRepository klijentRepository;
    private final KorisnikRepository korisnikRepository;

    public RacunKontroler(RacunRepository racunRepository, KlijentRepository klijentRepository,
                           KorisnikRepository korisnikRepository) {
        this.racunRepository = racunRepository;
        this.klijentRepository = klijentRepository;
        this.korisnikRepository = korisnikRepository;
    }

    private Tvrtka dohvatiTvrtkuPrijavljenogKorisnika(Authentication authentication) {
        String email = authentication.getName();
        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Prijavljeni korisnik s emailom " + email + " ne postoji"));
        return korisnik.getTvrtka();
    }

    @GetMapping("/racuni")
    public String prikaziRacune(Model model, Authentication authentication,
                                 @RequestParam(required = false) Long klijentId,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String datumOd,
                                 @RequestParam(required = false) String datumDo) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);

        StatusRacuna odabraniStatus = parsirajStatus(status);
        LocalDate pocetakRazdoblja = parsirajDatum(datumOd);
        LocalDate krajRazdoblja = parsirajDatum(datumDo);

        List<Racun> racuni = racunRepository.pretraziRacune(
                tvrtka, klijentId, odabraniStatus, pocetakRazdoblja, krajRazdoblja,
                Sort.by("datumIzdavanja").descending());

        model.addAttribute("racuni", racuni);
        model.addAttribute("klijenti", klijentRepository.findByTvrtka(tvrtka));
        model.addAttribute("klijentId", klijentId);
        model.addAttribute("status", status);
        model.addAttribute("datumOd", datumOd);
        model.addAttribute("datumDo", datumDo);

        return "racuni";
    }

    private StatusRacuna parsirajStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusRacuna.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate parsirajDatum(String datum) {
        if (datum == null || datum.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(datum);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/racuni/novi")
    public String prikaziFormu(Model model, Authentication authentication) {
        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);

        model.addAttribute("klijenti", klijentRepository.findByTvrtka(tvrtka));
        model.addAttribute("danas", LocalDate.now());

        return "novi-racun";
    }

    @PostMapping("/racuni")
    public String dodajRacun(@RequestParam Long klijentId,
                              @RequestParam String datumIzdavanja,
                              @RequestParam String datumDospijeca,
                              @RequestParam(required = false) String napomena,
                              @RequestParam List<String> opis,
                              @RequestParam List<@DecimalMin(value = "0.01", message = "Količina mora biti veća od 0") BigDecimal> kolicina,
                              @RequestParam List<@DecimalMin(value = "0.0", message = "Jedinična cijena ne smije biti negativna") BigDecimal> jedinicnaCijena,
                              @RequestParam List<@DecimalMin(value = "0.0", message = "PDV stopa ne smije biti negativna")
                                      @DecimalMax(value = "100.0", message = "PDV stopa ne smije biti veća od 100%") BigDecimal> pdvStopa,
                              Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Klijent klijent = klijentRepository.findByIdAndTvrtka(klijentId, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + klijentId + " ne postoji"));

        LocalDate datumIzd = LocalDate.parse(datumIzdavanja);
        LocalDate datumDosp = LocalDate.parse(datumDospijeca);

        if (datumDosp.isBefore(datumIzd)) {
            throw new NevazeciPodaciException("Datum dospijeća ne smije biti prije datuma izdavanja");
        }

        String brojRacuna = generirajBrojRacuna(tvrtka, datumIzd);

        Racun racun = new Racun(tvrtka, klijent, brojRacuna, datumIzd, datumDosp, napomena);

        for (int i = 0; i < opis.size(); i++) {
            if (opis.get(i) == null || opis.get(i).isBlank()) {
                continue;
            }
            racun.dodajStavku(new StavkaRacuna(opis.get(i), kolicina.get(i), jedinicnaCijena.get(i), pdvStopa.get(i)));
        }

        racunRepository.save(racun);

        return "redirect:/racuni/" + racun.getId();
    }

    private String generirajBrojRacuna(Tvrtka tvrtka, LocalDate datumIzdavanja) {
        int godina = datumIzdavanja.getYear();
        LocalDate pocetakGodine = LocalDate.of(godina, 1, 1);
        LocalDate krajGodine = LocalDate.of(godina, 12, 31);

        long brojRacunaOveGodine = racunRepository
                .countByTvrtkaAndDatumIzdavanjaBetween(tvrtka, pocetakGodine, krajGodine);

        int sljedeciBroj = (int) brojRacunaOveGodine + 1;

        return godina + "-" + String.format("%04d", sljedeciBroj);
    }

    @GetMapping("/racuni/{id}")
    public String prikaziPregled(@PathVariable Long id, Model model, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Racun racun = racunRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Račun s id " + id + " ne postoji"));

        model.addAttribute("racun", racun);

        return "pregled-racun";
    }

    @PostMapping("/racuni/{id}/status")
    public String promijeniStatus(@PathVariable Long id, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Racun racun = racunRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Račun s id " + id + " ne postoji"));

        if (racun.getStatus() == StatusRacuna.STORNIRAN) {
            throw new NevazeciPodaciException("Stornirani račun se više ne može mijenjati");
        }

        racun.setStatus(racun.getStatus() == StatusRacuna.PLACEN ? StatusRacuna.NEPLACEN : StatusRacuna.PLACEN);
        racunRepository.save(racun);

        return "redirect:/racuni/" + id;
    }

    // Izdan račun se ne briše iz baze (time bi se izgubila povijest izdanih dokumenata),
    // nego se trajno označi kao storniran - isključen je iz izvještaja o prihodu, ali ostaje vidljiv.
    @PostMapping("/racuni/{id}/storniraj")
    public String stornirajRacun(@PathVariable Long id, Authentication authentication) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        Racun racun = racunRepository.findByIdAndTvrtka(id, tvrtka)
                .orElseThrow(() -> new IllegalArgumentException("Račun s id " + id + " ne postoji"));

        racun.setStatus(StatusRacuna.STORNIRAN);
        racunRepository.save(racun);

        return "redirect:/racuni/" + id;
    }
}
