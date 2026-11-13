package com.cinemauq.patterns.behavioral;

public class SelectorPoliticaCancelacion {
    public static PoliticaCancelacion seleccionar(long horasRestantes) {
        if (horasRestantes > 24) {
            return new PoliticaMas24h();
        } else if (horasRestantes >= 2) {
            return new PoliticaEntre2y24h();
        } else {
            return new PoliticaMenos2h();
        }
    }
}
