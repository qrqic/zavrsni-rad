package com.grgic.zavrsni.security;

import com.grgic.zavrsni.repository.KorisnikRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class NevazecaSesijaFilter extends OncePerRequestFilter {

    private final KorisnikRepository korisnikRepository;

    public NevazecaSesijaFilter(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean prijavljen = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        if (prijavljen && korisnikRepository.findByEmail(authentication.getName()).isEmpty()) {

            new SecurityContextLogoutHandler().logout(request, response, authentication);
            obrisiSesijskiCookie(response);

            response.sendRedirect(request.getContextPath() + "/login?sesija-istekla");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void obrisiSesijskiCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
