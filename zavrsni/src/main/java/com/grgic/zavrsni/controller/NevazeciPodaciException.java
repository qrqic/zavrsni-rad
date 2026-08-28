package com.grgic.zavrsni.controller;

public class NevazeciPodaciException extends RuntimeException {

    public NevazeciPodaciException(String poruka) {
        super(poruka);
    }
}
