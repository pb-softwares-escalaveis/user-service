package com.br.infnet.userservice.penalty;

import org.springframework.stereotype.Component;

@Component
public class PenaltyFactory {
    private final Suspensao15DiasStrategy suspensao15Dias;
    private final Suspensao30DiasStrategy suspensao30Dias;
    private final BanimentoStrategy banimento;

    public PenaltyFactory(Suspensao15DiasStrategy suspensao15Dias,
                          Suspensao30DiasStrategy suspensao30Dias,
                          BanimentoStrategy banimento) {
        this.suspensao15Dias = suspensao15Dias;
        this.suspensao30Dias = suspensao30Dias;
        this.banimento = banimento;
    }

    public PenaltyStrategy getStrategy(int marksAtuais) {
        return switch (marksAtuais) {
            case 3 -> suspensao15Dias;
            case 2 -> suspensao30Dias;
            case 1 -> banimento;
            default -> throw new IllegalArgumentException("Marks inválidas: " + marksAtuais);
        };
    }
}