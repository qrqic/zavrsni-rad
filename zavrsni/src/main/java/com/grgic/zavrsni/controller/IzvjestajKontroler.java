package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Korisnik;
import com.grgic.zavrsni.model.Racun;
import com.grgic.zavrsni.model.StatusRacuna;
import com.grgic.zavrsni.model.Tvrtka;
import com.grgic.zavrsni.repository.KlijentRepository;
import com.grgic.zavrsni.repository.KorisnikRepository;
import com.grgic.zavrsni.repository.RacunRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class IzvjestajKontroler {

    private final RacunRepository racunRepository;
    private final KlijentRepository klijentRepository;
    private final KorisnikRepository korisnikRepository;

    public IzvjestajKontroler(RacunRepository racunRepository, KlijentRepository klijentRepository,
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

    @GetMapping("/izvjestaji")
    public String prikaziIzvjestaj(Model model, Authentication authentication,
                                    @RequestParam(required = false) Long klijentId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String datumOd,
                                    @RequestParam(required = false) String datumDo) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        List<Racun> racuni = dohvatiFiltriraneRacune(tvrtka, klijentId, status, datumOd, datumDo);

        BigDecimal ukupno = racuni.stream()
                .filter(racun -> racun.getStatus() != StatusRacuna.STORNIRAN)
                .map(Racun::getUkupanIznos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> sumePoKlijentu = new LinkedHashMap<>();
        racuni.stream()
                .filter(racun -> racun.getStatus() != StatusRacuna.STORNIRAN)
                .forEach(racun -> sumePoKlijentu.merge(racun.getKlijent().getNaziv(), racun.getUkupanIznos(), BigDecimal::add));

        List<Map.Entry<String, BigDecimal>> poKlijentu = sumePoKlijentu.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .toList();

        model.addAttribute("racuni", racuni);
        model.addAttribute("ukupno", ukupno);
        model.addAttribute("poKlijentu", poKlijentu);
        model.addAttribute("klijenti", klijentRepository.findByTvrtka(tvrtka));
        model.addAttribute("klijentId", klijentId);
        model.addAttribute("status", status);
        model.addAttribute("datumOd", datumOd);
        model.addAttribute("datumDo", datumDo);

        return "izvjestaji";
    }

    @GetMapping("/izvjestaji/izvoz")
    public ResponseEntity<byte[]> izvezi(Authentication authentication,
                                          @RequestParam(required = false) Long klijentId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String datumOd,
                                          @RequestParam(required = false) String datumDo) {

        Tvrtka tvrtka = dohvatiTvrtkuPrijavljenogKorisnika(authentication);
        List<Racun> racuni = dohvatiFiltriraneRacune(tvrtka, klijentId, status, datumOd, datumDo);

        String csv = izgradiCsv(racuni, parsirajDatum(datumOd), parsirajDatum(datumDo));

        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] tijelo = csv.getBytes(StandardCharsets.UTF_8);
        byte[] sadrzaj = new byte[bom.length + tijelo.length];
        System.arraycopy(bom, 0, sadrzaj, 0, bom.length);
        System.arraycopy(tijelo, 0, sadrzaj, bom.length, tijelo.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"izvjestaj-prihodi.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(sadrzaj);
    }

    private String izgradiCsv(List<Racun> racuni, LocalDate datumOd, LocalDate datumDo) {
        DateTimeFormatter formatDatuma = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        BigDecimal ukupno = racuni.stream()
                .filter(racun -> racun.getStatus() != StatusRacuna.STORNIRAN)
                .map(Racun::getUkupanIznos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder csv = new StringBuilder();
        csv.append(csvRedak("Izvještaj o prihodima")).append("\r\n");
        csv.append(csvRedak("Razdoblje: " + opisRazdoblja(datumOd, datumDo, formatDatuma))).append("\r\n");
        csv.append("\r\n");
        csv.append(csvRedak("Broj računa", "Klijent", "Datum izdavanja", "Datum dospijeća", "Status", "Iznos (EUR)")).append("\r\n");

        for (Racun racun : racuni) {
            csv.append(csvRedak(
                    racun.getBrojRacuna(),
                    racun.getKlijent().getNaziv(),
                    formatDatuma.format(racun.getDatumIzdavanja()),
                    formatDatuma.format(racun.getDatumDospijeca()),
                    formatStatus(racun.getStatus()),
                    formatIznos(racun.getUkupanIznos())
            )).append("\r\n");
        }

        csv.append("\r\n");
        csv.append(csvRedak("", "", "", "", "Ukupno", formatIznos(ukupno))).append("\r\n");

        return csv.toString();
    }

    private String formatStatus(StatusRacuna status) {
        return switch (status) {
            case PLACEN -> "Plaćeno";
            case STORNIRAN -> "Stornirano";
            case NEPLACEN -> "Neplaćeno";
        };
    }

    private String opisRazdoblja(LocalDate datumOd, LocalDate datumDo, DateTimeFormatter formatDatuma) {
        if (datumOd == null && datumDo == null) {
            return "svi računi";
        }
        if (datumOd != null && datumDo != null) {
            return formatDatuma.format(datumOd) + " - " + formatDatuma.format(datumDo);
        }
        if (datumOd != null) {
            return "od " + formatDatuma.format(datumOd);
        }
        return "do " + formatDatuma.format(datumDo);
    }

    private String formatIznos(BigDecimal iznos) {
        return iznos.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }

    private String csvRedak(String... polja) {
        return Arrays.stream(polja)
                .map(polje -> "\"" + (polje == null ? "" : polje.replace("\"", "\"\"")) + "\"")
                .collect(Collectors.joining(";"));
    }

    private List<Racun> dohvatiFiltriraneRacune(Tvrtka tvrtka, Long klijentId, String status,
                                                 String datumOd, String datumDo) {
        StatusRacuna odabraniStatus = parsirajStatus(status);
        LocalDate pocetakRazdoblja = parsirajDatum(datumOd);
        LocalDate krajRazdoblja = parsirajDatum(datumDo);

        return racunRepository.pretraziRacune(tvrtka, klijentId, odabraniStatus, pocetakRazdoblja, krajRazdoblja,
                Sort.by("datumIzdavanja").descending());
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
}
