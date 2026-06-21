package com.br.infnet.userservice.penalty;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.metrics.UserMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class Suspensao30DiasStrategy implements PenaltyStrategy {
    private final UserMetrics userMetrics;

    public Suspensao30DiasStrategy(UserMetrics userMetrics) {
        this.userMetrics = userMetrics;
    }

    @Override
    public void aplicar(Usuario usuario, Reputacao reputacao, String reason, Instant ocorridoEm) {
        usuario.setStatus(Status.SUSPENSO);
        reputacao.setMarks(1);
        reputacao.setDataUltimaPunicao(ocorridoEm);
        Instant suspensoAte = ocorridoEm.plus(30, ChronoUnit.DAYS);
        reputacao.setSuspensoAte(suspensoAte);
        userMetrics.incrementUsersSuspended();
    }

    @Override
    public boolean deveEmitirEventoDeSuspensao() {
        return true;
    }

    @Override
    public String getLogMessage(Usuario usuario) {
        return String.format("Usuário %s suspenso por 30 dias (marks 2→1). Até: %s",
                usuario.getUsername(), usuario.getReputacao().getSuspensoAte());
    }
}