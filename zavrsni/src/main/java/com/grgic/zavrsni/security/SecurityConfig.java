package com.grgic.zavrsni.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    private final NevazecaSesijaFilter nevazecaSesijaFilter;

    public SecurityConfig(NevazecaSesijaFilter nevazecaSesijaFilter) {
        this.nevazecaSesijaFilter = nevazecaSesijaFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(zahtjevi -> zahtjevi
                        .requestMatchers("/registracija", "/login", "/css/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/klijenti", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .addFilterAfter(nevazecaSesijaFilter, AuthorizationFilter.class);

        return http.build();
    }
}
