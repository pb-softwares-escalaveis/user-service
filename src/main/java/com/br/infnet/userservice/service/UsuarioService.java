package com.br.infnet.userservice.service;

import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.*;
import com.br.infnet.userservice.dto.events.UserCreatedEvent;
import com.br.infnet.userservice.dto.events.UserDeletedEvent;
import com.br.infnet.userservice.enums.Status;
import com.br.infnet.userservice.exceptions.UsuarioNaoAutenticadoException;
import com.br.infnet.userservice.exceptions.UsuarioNotFoundException;
import com.br.infnet.userservice.exceptions.UsuarioMenorDeIdadeException;
import com.br.infnet.userservice.kafka.UserKafkaProducer;
import com.br.infnet.userservice.mapper.UsuarioMapper;
import com.br.infnet.userservice.repository.UsuarioRepository;
import com.br.infnet.userservice.utils.CorrelationIdUtil;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import com.br.infnet.userservice.utils.UsernameGenerator;
import org.springframework.transaction.annotation.Transactional;
import com.br.infnet.userservice.mapper.UserEventMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final Keycloak keycloak;
    private final UserKafkaProducer kafkaProducer;
    private final UserEventMapper userEventMapper;

    @Value("${keycloak.realm}")
    private String realm;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper, Keycloak keycloak, UserKafkaProducer kafkaProducer, UserEventMapper userEventMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.keycloak = keycloak;
        this.kafkaProducer = kafkaProducer;
        this.userEventMapper = userEventMapper;
    }

    @Cacheable(value = "perfil", key = "#id")
    public UsuarioProfileResponse getUsuarioProfileById(UUID id) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Consultando perfil de usuário para ID: {}", correlationId, id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            throw new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        return usuarioMapper.toProfileResponse(usuario);
    }

    @Cacheable(value = "user-info", key = "#id")
    public UsuarioResponse getUsuarioInfoById(UUID id) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Consultando dados de usuário para pagamento", correlationId);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            throw new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        log.info("[{}] Dados para pagamento encontrados para usuário de ID: {}", correlationId, id);
        return usuarioMapper.toResponse(usuario);
    }

    public List<UsuarioStatusResponse> getUsuariosStatusByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Consultando status de usuários de IDs: {}", correlationId, ids);

        List<UUID> uniqueIds = ids.stream().distinct().toList();
        List<Usuario> usuarios = usuarioRepository.findAllById(uniqueIds);

        return usuarios.stream()
                .filter(usuario -> usuario.getStatus() != Status.INATIVO)
                .map(usuario -> new UsuarioStatusResponse(
                        usuario.getId(),
                        usuario.getStatus(),
                        isAllowed(usuario)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vendedor-info", key = "#id")
    public VendedorResponseInfo getVendedorInfoById(UUID id) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            throw new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        log.info("[{}] Consultando informações de vendedor para usuário ID: {}", correlationId, id);
        return usuarioMapper.toVendedorResponseInfo(usuario);
    }

    @Transactional
    public UsuarioCreationResponse criarUsuario(UsuarioCreationRequest request) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Iniciando criação de usuário: {}", correlationId, request.email());
        LocalDate dataAtual = LocalDate.now();
        LocalDate dataNascimento = request.dataNascimento();

        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username já existe: " + request.username());
        }

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.email());
        }

        if(usuarioRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + request.cpf());
        }

        if (Period.between(dataNascimento, dataAtual).getYears() < 18) {
            throw new UsuarioMenorDeIdadeException("Cadastro não permitido: Você deve ter 18 anos ou mais.");
        }

        UUID keycloakId = criarUsuarioViaKeycloakAPI(request);
        log.info("[{}] Usuário criado no Keycloak: {}", correlationId, keycloakId);

        Usuario novoUsuario = usuarioMapper.toEntity(request);
        novoUsuario.setId(keycloakId);
        usuarioRepository.save(novoUsuario);
        log.info("[{}] Usuário salvo no banco: {}", correlationId, novoUsuario.getId());

        UserCreatedEvent eventoCriacao = userEventMapper.toUserCreatedEvent(novoUsuario);

       try {
            kafkaProducer.sendUserCreated(eventoCriacao).get(20, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar evento de criação para o Kafka", e);
        }
        return new UsuarioCreationResponse(novoUsuario.getId());
    }

    @Transactional
    @CacheEvict(value = {"perfil", "user-info", "vendedor-info"}, key = "#id")
    public void deletarUsuario(UUID id) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Iniciando deleção de usuário: {}", correlationId, id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        if (usuario.getStatus() == Status.INATIVO) {
            return;
        }
        usuario.setStatus(Status.INATIVO);
        usuario.setDataAtualizacao(Instant.now());
        usuarioRepository.save(usuario);

        UserDeletedEvent eventoDelecao = new UserDeletedEvent(
                UUID.randomUUID(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Instant.now()
        );

        try {
            kafkaProducer.sendUserDeleted(eventoDelecao).get(20, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar evento de deleção para o Kafka", e);
        }
    }

    @Transactional
    @CacheEvict(value = "perfil", allEntries = true)
    public void alterarFotoDePerfil(String urlNovaFoto) {
        UUID correlationId = CorrelationIdUtil.getCorrelationIdAsUUID();
        log.info("[{}] Iniciando alteração de foto de perfil", correlationId);
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UsuarioNaoAutenticadoException("Usuário não autenticado!");
        }
        String keycloakId = auth.getToken().getClaimAsString("sub");


        Usuario usuario = usuarioRepository.findById(UUID.fromString(keycloakId))
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado no banco de dados!"));

        usuario.setFotoPerfil(urlNovaFoto);
        usuario.setDataAtualizacao(Instant.now());
        log.info("Alterada foto de perfil para usuário de ID: {}", keycloakId);

        usuarioRepository.save(usuario);
    }

    public List<String> sugerirUsernamesDisponiveis(String nomeBase, int quantidadeDesejada) {
        List<String> validos = new ArrayList<>();
        int tentativasIniciais = quantidadeDesejada * 2;

        List<String> gerados = UsernameGenerator.gerarSugestoes(nomeBase, tentativasIniciais);

        for (String username : gerados) {
            if (!usuarioRepository.existsByUsername(username)) {
                validos.add(username);
            }
            if (validos.size() == quantidadeDesejada) {
                break;
            }
        }
        return validos;
    }
    /*public void solicitarResetDeSenha(String email) {
        List<UserRepresentation> usuariosIdp = keycloak.realm(realm).users().search(null, null, null, email, 0, 1);

        if (usuariosIdp.isEmpty()) {
            return;
        }

        String userIdIdp = usuariosIdp.getFirst().getId();
        UserResource userResource = keycloak.realm(realm).users().get(userIdIdp);

        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    public void alterarPropriaSenha(String senhaNova) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String userIdIdp = auth.getToken().getClaimAsString("sub");

        UserResource userResource = keycloak.realm(realm).users().get(userIdIdp);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senhaNova);
        credential.setTemporary(false);

        userResource.resetPassword(credential);
    }


    public void forcarConfiguracaoMFA() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String userIdIdp = auth.getToken().getClaimAsString("sub");

        UserResource userResource = keycloak.realm(realm).users().get(userIdIdp);
        userResource.executeActionsEmail(List.of("CONFIGURE_TOTP"));
    }*/

    private UUID criarUsuarioViaKeycloakAPI(UsuarioCreationRequest request) {
        UserRepresentation userKc = getUserRepresentation(request);

        UsersResource usersResource = keycloak.realm(realm).users();

        try (Response response = usersResource.create(userKc)) {

            if (response.getStatus() != 201) {
                throw new RuntimeException("Falha na criação do usuário no Keycloak! Http "
                        + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
                        + ". Verifique se o e-mail/usuário já não existe.");
            }

            String locationUrl = response.getHeaderString("Location");
            String idCriado = locationUrl.substring(locationUrl.lastIndexOf('/') + 1);
            return UUID.fromString(idCriado);

        } catch (Exception e) {
            throw new RuntimeException("Erro durante sincronização com o Keycloak: " + e.getMessage());
        }
    }

    //**METODOS AUXILIARES **//
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

    private boolean isAllowed(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.getStatus() == Status.ATIVO;
    }
}
