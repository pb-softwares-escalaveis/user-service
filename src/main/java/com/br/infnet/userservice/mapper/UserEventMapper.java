package com.br.infnet.userservice.mapper;

import com.br.infnet.userservice.domain.Endereco;
import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.UserCreatedEvent;
import com.br.infnet.userservice.utils.CorrelationIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UserEventMapper {
    public UserCreatedEvent toUserCreatedEvent(Usuario usuario) {
        if (usuario == null) {
            log.warn("Tentativa de criar evento para usuário nulo");
            return null;
        }

        List<Endereco> enderecos = usuario.getEnderecos();
        Endereco enderecoPrincipal = (enderecos != null && !enderecos.isEmpty()) ? enderecos.getFirst() : null;
        Reputacao reputacao = usuario.getReputacao();
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("Evento de criação de usuário mapeado para usuário {}", usuario.getId());

        return new UserCreatedEvent(
                correlationId,
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getUsername(),
                usuario.getFotoPerfil(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getTelefone(),
                reputacao != null ? reputacao.getNota() : 5.0f,
                enderecoPrincipal != null ? enderecoPrincipal.getPais() : null,
                enderecoPrincipal != null ? enderecoPrincipal.getEstado() : null,
                enderecoPrincipal != null ? enderecoPrincipal.getCidade() : null,
                enderecoPrincipal != null ? enderecoPrincipal.getCep() : null,
                Instant.now()
        );
    }
}