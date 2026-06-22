package com.br.infnet.userservice.penalty;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class Suspensao15DiasStrategy implements PenaltyStrategy {
    @Override
    public void aplicar(Usuario usuario, Reputacao reputacao, String reason, Instant ocorridoEm) {
        usuario.setStatus(Status.SUSPENSO);
        reputacao.setMarks(2);
        reputacao.setDataUltimaPunicao(ocorridoEm);
        Instant suspensoAte = ocorridoEm.plus(15, ChronoUnit.DAYS);
        reputacao.setSuspensoAte(suspensoAte);
    }

    @Override
    public boolean deveEmitirEventoDeSuspensao() {
        return true;
    }

    @Override
    public String getLogMessage(Usuario usuario) {
        return String.format("Usuário %s suspenso por 15 dias (marks 3→2). Até: %s",
                usuario.getUsername(), usuario.getReputacao().getSuspensoAte());
    }
}