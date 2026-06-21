package com.br.infnet.userservice.penalty;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.metrics.UserMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class BanimentoStrategy implements PenaltyStrategy {
    private final UserMetrics userMetrics;

    public BanimentoStrategy(UserMetrics userMetrics) {
        this.userMetrics = userMetrics;
    }

    @Override
    public void aplicar(Usuario usuario, Reputacao reputacao, String reason, Instant ocorridoEm) {
        usuario.setStatus(Status.BANIDO);
        reputacao.setMarks(0);
        reputacao.setDataUltimaPunicao(ocorridoEm);
        reputacao.setSuspensoAte(null);
        userMetrics.incrementUsersBanned();
    }

    @Override
    public boolean deveEmitirEventoDeSuspensao() {
        return false;
    }

    @Override
    public String getLogMessage(Usuario usuario) {
        return String.format("Usuário %s banido permanentemente (marks 1 → 0)", usuario.getUsername());
    }
}
