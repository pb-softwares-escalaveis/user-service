package com.br.infnet.userservice.service;

import com.br.infnet.userservice.domain.Reputacao;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.UsuarioCreationRequest;
import com.br.infnet.userservice.dto.UsuarioProfileResponse;
import com.br.infnet.userservice.dto.UsuarioStatusResponse;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.EntidadeNotFoundException;
import com.br.infnet.userservice.exceptions.UsuarioMenorDeIdadeException;
import com.br.infnet.userservice.mapper.UsuarioMapper;
import com.br.infnet.userservice.repository.UsuarioRepository;
import jakarta.ws.rs.core.Response;
import org.jspecify.annotations.NonNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Collections;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper, Keycloak keycloak) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.keycloak = keycloak;
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

        String keycloakId = criarUsuarioViaKeycloakAPI(request);

        Usuario novoUsuario = usuarioMapper.toEntity(request);
        novoUsuario.setKeycloakId(keycloakId);

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

    private String criarUsuarioViaKeycloakAPI(UsuarioCreationRequest request) {
        UserRepresentation userKc = getUserRepresentation(request);

        UsersResource usersResource = keycloak.realm(realm).users();

        try (Response response = usersResource.create(userKc)) {

            if (response.getStatus() != 201) {
                throw new RuntimeException("Falha de Criação Externa no IdP Keycloak! Http "
                        + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
                        + ". Verifique se o e-mail/usuário já não existe.");
            }

            String locationUrl = response.getHeaderString("Location");
            return locationUrl.substring(locationUrl.lastIndexOf('/') + 1);

        } catch (Exception e) {
            throw new RuntimeException("Erro inibitivo durante sincronização com IdP: " + e.getMessage());
        }
    }

    private static @NonNull UserRepresentation getUserRepresentation(UsuarioCreationRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.senha());
        credential.setTemporary(false);

        UserRepresentation userKc = new UserRepresentation();
        userKc.setUsername(request.username());
        userKc.setEmail(request.email());
        userKc.setFirstName(request.nome());
        userKc.setLastName(request.sobrenome());
        userKc.setEnabled(true);
        userKc.setCredentials(Collections.singletonList(credential));
        return userKc;
    }
}
