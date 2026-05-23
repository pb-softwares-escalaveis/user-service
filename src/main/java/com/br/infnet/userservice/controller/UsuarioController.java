package com.br.infnet.userservice.controller;

import com.br.infnet.userservice.dto.UsuarioResponseDTO;
import com.br.infnet.userservice.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/usuarios")
public class UsuarioController {
    public UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping()
    public List<UsuarioResponseDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id);
    }
}
