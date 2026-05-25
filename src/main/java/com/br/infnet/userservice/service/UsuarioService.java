package com.br.infnet.userservice.service;

import com.br.infnet.userservice.domain.Auth;
import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.UsuarioCreationRequest;
import com.br.infnet.userservice.dto.UsuarioProfileResponse;
import com.br.infnet.userservice.dto.UsuarioStatusResponse;
import com.br.infnet.userservice.enums.ModoVerificacao;
import com.br.infnet.userservice.enums.Role;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.EntidadeNotFoundException;
import com.br.infnet.userservice.exceptions.UsuarioMenorDeIdadeException;
import com.br.infnet.userservice.mapper.UsuarioMapper;
import com.br.infnet.userservice.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioProfileResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            throw new EntidadeNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        return usuarioMapper.toProfileResponse(usuario);
    }

    public UsuarioStatusResponse getUsuarioStatusById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            throw new EntidadeNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        return new UsuarioStatusResponse(usuario.getStatus());
    }

    public void criarUsuario(UsuarioCreationRequest request) {
        LocalDate dataAtual = LocalDate.now();
        LocalDate dataNascimento = request.dataNascimento();

        if (Period.between(dataNascimento, dataAtual).getYears() < 18) {
            throw new UsuarioMenorDeIdadeException("Cadastro não permitido: O usuário deve ter 18 anos ou mais.");
        }

        Usuario novoUsuario = usuarioMapper.toEntity(request);

        Auth auth = new Auth();
        auth.setHashSenha(passwordEncoder.encode(request.senha()));
        auth.setRole(Role.USER);
        auth.setVerificado(false);
        auth.setModoVerificacao(ModoVerificacao.PENDENTE);
        auth.setVerificacaoDuasEtapas(false);
        auth.setUsuario(novoUsuario);
        novoUsuario.setAuth(auth);

        Reputacao reputacao = new Reputacao();
        reputacao.setMarks(3);
        reputacao.setReputacao(5.0f);
        reputacao.setDataUltimaPunicao(null);
        reputacao.setUsuario(novoUsuario);
        novoUsuario.setReputacao(reputacao);

        usuarioRepository.save(novoUsuario);
    }

    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNotFoundException("Usuário não encontrado com o ID: " + id));

        if (usuario.getStatus() == Status.INATIVO) {
            return;
        }
        usuario.setStatus(Status.INATIVO);
        usuario.setDataAtualizacao(OffsetDateTime.now());
        usuarioRepository.save(usuario);
    }
}