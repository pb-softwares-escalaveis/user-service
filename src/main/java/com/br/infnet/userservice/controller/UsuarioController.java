package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.dto.*;
import com.br.infnet.userservice.service.UsuarioService;
import com.br.infnet.userservice.storage.BucketStorageService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Profile({"test", "dev"})
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final BucketStorageService storageService;

    public UsuarioController(UsuarioService usuarioService, BucketStorageService storageService) {
        this.usuarioService = usuarioService;
        this.storageService = storageService;
    }
    @GetMapping("/me")
    public Map<String, String> me(@RequestHeader("X-User-Id") String userId,
                                  @RequestHeader("X-User-Email") String email,
                                  @RequestHeader("X-User-Nome") String nome,
                                  @RequestHeader("X-User-Status") String status,
                                  @RequestHeader("X-User-Allowed") String allowed) {
        return Map.of("userId", userId, "email", email, "nome", nome, "status", status, "allowed", allowed);
    }

    //**CRUD USUÁRIO**//
    @PostMapping("/novo")
    public ResponseEntity<UsuarioCreationResponse> criarUsuario(@Valid @RequestBody UsuarioCreationRequest request) {
        UsuarioCreationResponse response = usuarioService.criarUsuario(request);
        URI location = URI.create("/usuarios/" + response.userId() + "/perfil");
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<UsuarioProfileResponse> getUsuarioProfileById(@PathVariable UUID id) {
        UsuarioProfileResponse response = usuarioService.getUsuarioProfileById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletarUsuario(@PathVariable UUID id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.ok(new MessageResponse("Usuário desativado com sucesso."));
    }

    //**AVATARES**//
    @GetMapping("/listar-pfps")
    public ResponseEntity<List<String>> listarAvatares() {
        List<String> imagens = storageService.listarImagensDisponiveis();
        return ResponseEntity.ok(imagens);
    }

    @PutMapping("/trocar-pfp")
    public ResponseEntity<Void> alterarFotoDePerfil(@RequestBody String linkFoto) {
        if (linkFoto == null || linkFoto.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        usuarioService.alterarFotoDePerfil(linkFoto);
        return ResponseEntity.ok().build();
    }

    //**USERNAMES**//
    @GetMapping("/listar-usernames")
    public ResponseEntity<List<String>> obterSugestoes(@RequestParam String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> sugestoes = usuarioService.sugerirUsernamesDisponiveis(nome, 5);
        return ResponseEntity.ok(sugestoes);
    }


    //**CREDENCIAIS**//
    /*@PostMapping("/esqueci-senha")
    public ResponseEntity<Void> solicitarResetDeSenha(@RequestParam String email) {
        usuarioService.solicitarResetDeSenha(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody String novaSenha) {
        usuarioService.alterarPropriaSenha(novaSenha);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ativar-2fa")
    public ResponseEntity<Void> pedirHabilitacao2FA() {
        usuarioService.forcarConfiguracaoMFA();
        return ResponseEntity.ok().build();
    }*/

    //**ENDPOINTS PARA OUTROS MICROSSERVIÇOS **//
    @GetMapping("/status")
    public ResponseEntity<List<UsuarioStatusResponse>> getUsuariosStatusByIds(
            @RequestParam List<UUID> ids) {
        List<UsuarioStatusResponse> responses = usuarioService.getUsuariosStatusByIds(ids);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/seller-info")
    public ResponseEntity<VendedorResponseInfo> getVendedorInfoById(@PathVariable UUID id) {
        VendedorResponseInfo response = usuarioService.getVendedorInfoById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable UUID id) {
        UsuarioResponse response = usuarioService.getUsuarioInfoById(id);
        return ResponseEntity.ok(response);
    }


}