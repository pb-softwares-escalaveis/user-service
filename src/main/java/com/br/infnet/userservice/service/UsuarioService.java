package com.br.infnet.userservice.service;

import com.br.infnet.userservice.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    public List<UsuarioResponseDTO> getAllUsuarios() {
        return List.of(new UsuarioResponseDTO(1L, "João", "Silva", "joaosilva", "dasda@fsdfui.com", "12345678900", "11987654321", 3, 4.7f),
                new UsuarioResponseDTO(2L, "Maria", "Souza", "mariasouza", "sfkjdhu@kdhfkjdah.com.br", "98765432100", "11912345678", 2, 2.8f));
    }

    public UsuarioResponseDTO getUsuarioById(Long id) {
        return new UsuarioResponseDTO(id, "João", "Silva", "joaosilva", "erw3yrey@wdbfuis.com", "12345678900", "11987654321", 3, 4.7f);
    }
}

