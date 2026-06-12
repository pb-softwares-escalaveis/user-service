package com.br.infnet.userservice.mapper;

import com.br.infnet.userservice.domain.Endereco;
import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.UserCreatedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class UserEventMapper {
    public UserCreatedEvent toUserCreatedEvent(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        List<Endereco> enderecos = usuario.getEnderecos();
        Endereco enderecoPrincipal = (enderecos != null && !enderecos.isEmpty()) ? enderecos.getFirst() : null;
        Reputacao reputacao = usuario.getReputacao();

        return new UserCreatedEvent(
                UUID.randomUUID(),
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