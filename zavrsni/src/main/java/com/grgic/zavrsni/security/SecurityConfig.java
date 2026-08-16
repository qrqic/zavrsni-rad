package com.grgic.zavrsni.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration -> Spring cita ovu klasu pri pokretanju i registrira sve
// sto vraca metoda oznacena s @Bean kao dio aplikacije.
@Configuration
public class SecurityConfig {

    // BCrypt je jednosmjerni algoritam hashiranja lozinki (ne moze se "dehashirati").
    // Ovaj bean koristimo i kod registracije (hashiranje prije spremanja u bazu)
    // i automatski ga koristi Spring Security kod provjere lozinke prilikom logina.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Ovdje definiramo koje rute su javne, a koje traze prijavu.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(zahtjevi -> zahtjevi
                        // /registracija i /login moraju biti dostupni svima -
                        // logicno, ne mozes se prijaviti ako ti prijava trazi prijavu.
                        .requestMatchers("/registracija", "/login").permitAll()
                        // sve ostalo (npr. /klijenti) trazi da si prijavljen
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        // nasa vlastita login stranica umjesto Spring Securityjeve zadane
                        .loginPage("/login")
                        // nakon uspjesne prijave, salji korisnika na popis klijenata
                        .defaultSuccessUrl("/klijenti", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}
