package com.grgic.zavrsni.controller;

import com.grgic.zavrsni.model.Klijent;
import com.grgic.zavrsni.repository.KlijentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class KlijentKontroler {

    private final KlijentRepository klijentRepository;

    public KlijentKontroler(KlijentRepository klijentRepository) {
        this.klijentRepository = klijentRepository;
    }

    @GetMapping("/klijenti")
    public String prikaziKlijente(Model model) {

        List<Klijent> klijenti = klijentRepository.findAll();

        model.addAttribute("klijenti", klijenti);

        return "klijenti";
    }

    @GetMapping("/klijenti/novi")
    public String prikaziFormu() {
        return "novi-klijent";
    }

    @PostMapping("/klijenti")
    public String dodajKlijenta(@RequestParam String naziv, @RequestParam String email) {

        Klijent noviKlijent = new Klijent(naziv, email);

        klijentRepository.save(noviKlijent);

        return "redirect:/klijenti";
    }


    @GetMapping("/klijenti/{id}/uredi")
    public String prikaziFormuZaUredivanje(@PathVariable Long id, Model model) {

        Klijent klijent = klijentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        model.addAttribute("klijent", klijent);

        return "uredi-klijent";
    }

    @PostMapping("/klijenti/{id}")
    public String azurirajKlijenta(@PathVariable Long id, @RequestParam String naziv, @RequestParam String email) {

        Klijent klijent = klijentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Klijent s id " + id + " ne postoji"));

        klijent.setNaziv(naziv);
        klijent.setEmail(email);

        klijentRepository.save(klijent);

        return "redirect:/klijenti";
    }


    @PostMapping("/klijenti/{id}/obrisi")
    public String obrisiKlijenta(@PathVariable Long id) {

        klijentRepository.deleteById(id);

        return "redirect:/klijenti";
    }
}
