package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.repository.UsuarioRepository;
import com.br.infnet.userservice.scheduler.BackFromSuspensionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test/scheduler")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class SchedulerTestController {

    private final UsuarioRepository usuarioRepository;
    private final BackFromSuspensionScheduler scheduler;

    @PostMapping("/restore/{userId}")
    public ResponseEntity<String> forceUnsuspend(@PathVariable UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getStatus() == Status.SUSPENSO) {
            usuario.setStatus(Status.ATIVO);
            if (usuario.getReputacao() != null) {
                usuario.getReputacao().setSuspensoAte(null);
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Usuário reativado com sucesso!");
        }
        return ResponseEntity.badRequest().body("Usuário não está suspenso");
    }

    @PostMapping("/run")
    public ResponseEntity<String> runSchedulerNow() {
        log.info("Executando scheduler manualmente via endpoint");
        scheduler.reativarUsuariosSuspensos();
        return ResponseEntity.ok("Scheduler executado!");
    }

    @GetMapping("/suspended")
    public ResponseEntity<List<Usuario>> getSuspendedUsers() {
        List<Usuario> suspended = usuarioRepository.findSuspendedUsers(Status.SUSPENSO);
        return ResponseEntity.ok(suspended);
    }

    @DeleteMapping("/suspension-date/{userId}")
    public ResponseEntity<String> clearSuspensionDate(@PathVariable UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getReputacao() != null) {
            usuario.getReputacao().setSuspensoAte(Instant.now().minusSeconds(1));
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Data de suspensão alterada para o passado");
        }
        return ResponseEntity.badRequest().body("Usuário não tem data de suspensão");
    }
}