package com.br.infnet.userservice.scheduler;

import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.UserBackFromSuspensionEvent;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.kafka.UserKafkaProducer;
import com.br.infnet.userservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackFromSuspensionScheduler {

    private final UsuarioRepository usuarioRepository;
    private final UserKafkaProducer kafkaProducer;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void reativarUsuariosSuspensos() {
        log.info("Iniciando verificação de usuários suspensos com período expirado");
        List<Usuario> usuariosParaReativar = usuarioRepository.findSuspendedUsersWithExpiredSuspension();

        if (usuariosParaReativar.isEmpty()) {
            log.info("Nenhum usuário com data de suspensão expirada encontrado");
            return;
        }

        for (Usuario usuario : usuariosParaReativar) {
            try {
                reativarUsuario(usuario);
            } catch (Exception e) {
                log.error("Erro ao reativar usuário {}: {}", usuario.getId(), e.getMessage(), e);
            }
        }
    }

    private void reativarUsuario(Usuario usuario) {
        usuario.setStatus(Status.ATIVO);
        if (usuario.getReputacao() != null) {
            usuario.getReputacao().setSuspensoAte(null);
        }
        usuarioRepository.save(usuario);

        log.info("Usuário {} (ID: {}) foi reativado", usuario.getUsername(), usuario.getId());

        UserBackFromSuspensionEvent event = new UserBackFromSuspensionEvent(
                UUID.randomUUID(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Instant.now()
        );

        try {
            kafkaProducer.sendUserUnsuspended(event);
            log.info("Evento de reativação enviado para o usuário {}", usuario.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar evento de reativação para o usuário {}", usuario.getId(), e);
        }
    }
}