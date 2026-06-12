package com.br.infnet.userservice.penalty;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;

import java.time.Instant;

public interface PenaltyStrategy {
    void aplicar(Usuario usuario, Reputacao reputacao, String reason, Instant ocorridoEm);
    boolean deveEmitirEventoDeSuspensao();
    String getLogMessage(Usuario usuario);
}