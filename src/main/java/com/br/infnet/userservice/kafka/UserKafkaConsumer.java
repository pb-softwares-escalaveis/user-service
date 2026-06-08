package com.br.infnet.userservice.kafka;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.events.UserBanEmittedEvent;
import com.br.infnet.userservice.dto.events.UserBannedEvent;
import com.br.infnet.userservice.dto.events.UserSuspendedEvent;
import com.br.infnet.userservice.dto.events.UserSuspensionEmittedEvent;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.UsuarioNotFoundException;
import com.br.infnet.userservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserKafkaConsumer {
    private final UsuarioRepository usuarioRepository;
    private final UserKafkaProducer kafkaProducer;

    @Transactional
    @KafkaListener(topics = "penalties.user.ban-emitted")
    public void consumeBan(UserBanEmittedEvent event) {
        log.info("Recebido banimento permanente para: {}", event.userId());
        updateUsuarioBanido(event);
    }

    @Transactional
    @KafkaListener(topics = "penalties.user.suspension-emitted")
    public void consumeSuspension(UserSuspensionEmittedEvent event) {
        log.info("Recebida suspensão para: {}", event.userId());
        int diasDeSuspensao = event.days();
        if (diasDeSuspensao == 15) {
            updateUsuarioSuspenso(event, 2);
        } else if (diasDeSuspensao == 30) {
            updateUsuarioSuspenso(event, 1);
        }
    }

    private void updateUsuarioSuspenso(UserSuspensionEmittedEvent event, int marks) {
        Usuario usuario = usuarioRepository.findById(event.userId())
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado: " + event.userId()));

        usuario.setStatus(Status.SUSPENSO);
        Reputacao reputacao = usuario.getReputacao();
        if (reputacao == null) {
            reputacao = new Reputacao();
            usuario.setReputacao(reputacao);
        }
        reputacao.setMarks(marks);
        reputacao.setDataUltimaPunicao(event.occurredAt());
        usuarioRepository.save(usuario);

        Instant suspendedUntil = event.occurredAt().plus(event.days(), ChronoUnit.DAYS);
        log.info("Usuário {} suspenso até {}", usuario.getUsername(), suspendedUntil);

        UserSuspendedEvent eventoSuspensao = new UserSuspendedEvent(
                UUID.randomUUID(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                event.reason(),
                event.occurredAt(),
                suspendedUntil
        );

        try {
            kafkaProducer.sendUserSuspended(eventoSuspensao).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar evento de suspensão para o Kafka", e);
        }
    }

    private void updateUsuarioBanido(UserBanEmittedEvent event) {
        Usuario usuario = usuarioRepository.findById(event.userId())
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado: " + event.userId()));

        usuario.setStatus(Status.BANIDO);
        Reputacao reputacao = usuario.getReputacao();
        if (reputacao == null) {
            reputacao = new Reputacao();
            usuario.setReputacao(reputacao);
        }
        reputacao.setMarks(0);
        reputacao.setDataUltimaPunicao(event.occurredAt());
        usuarioRepository.save(usuario);
        log.info("Usuário {} banido permanentemente", usuario.getUsername());

        UserBannedEvent eventoBanimento = new UserBannedEvent(
                UUID.randomUUID(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                event.reason(),
                event.occurredAt()
        );

        try {
            kafkaProducer.sendUserBanned(eventoBanimento).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar evento de banimento para o Kafka", e);
        }
    }
}