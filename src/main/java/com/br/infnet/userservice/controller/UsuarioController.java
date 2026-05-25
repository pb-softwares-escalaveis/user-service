package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.dto.UsuarioCreationRequest;
import com.br.infnet.userservice.dto.UsuarioProfileResponse;
import com.br.infnet.userservice.dto.UsuarioStatusResponse;
import com.br.infnet.userservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioProfileResponse> getUsuarioById(@PathVariable Long id) {
        UsuarioProfileResponse response = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<UsuarioStatusResponse> getUsuarioStatusById(@PathVariable Long id) {
        UsuarioStatusResponse response = usuarioService.getUsuarioStatusById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/novo")
    public ResponseEntity<Void> criarUsuario(@Valid @RequestBody UsuarioCreationRequest request) {
        usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}