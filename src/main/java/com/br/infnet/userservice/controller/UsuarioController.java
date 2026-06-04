package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.dto.*;
import com.br.infnet.userservice.service.UsuarioService;
import com.br.infnet.userservice.storage.BucketStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final BucketStorageService storageService;

    public UsuarioController(UsuarioService usuarioService, BucketStorageService storageService) {
        this.usuarioService = usuarioService;
        this.storageService = storageService;
    }

    //**CRUD USUÁRIO**//
    @PostMapping("/novo")
    public ResponseEntity<Void> criarUsuario(@Valid @RequestBody UsuarioCreationRequest request) {
        usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/{id}/perfil")
    public ResponseEntity<UsuarioProfileResponse> getUsuarioProfileById(@PathVariable UUID id) {
        UsuarioProfileResponse response = usuarioService.getUsuarioProfileById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable UUID id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
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
    @GetMapping("/{id}/status")
    public ResponseEntity<UsuarioStatusResponse> getUsuarioStatusById(@PathVariable UUID id) {
        UsuarioStatusResponse response = usuarioService.getUsuarioStatusById(id);
        return ResponseEntity.ok(response);
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