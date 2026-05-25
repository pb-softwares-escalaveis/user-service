package com.br.infnet.userservice.mapper;

import com.br.infnet.userservice.domain.Endereco;
import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.dto.UsuarioCreationRequest;
import com.br.infnet.userservice.dto.UsuarioProfileResponse;
import com.br.infnet.userservice.enums.Status;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class UsuarioMapper {

    // ==========================================
    // 1. Converter: Entidade -> ProfileResponse
    // ==========================================
    public UsuarioProfileResponse toProfileResponse(Usuario usuario) {
        if (usuario == null) return null;

        Float valorReputacao = null;
        if (usuario.getReputacao() != null) {
            valorReputacao = usuario.getReputacao().getReputacao();
        }

        return new UsuarioProfileResponse(
                usuario.getUsername(),
                usuario.getFotoPerfil(),
                valorReputacao
        );
    }

    // ==========================================
    // 2. Converter: UsuarioCreationRequest -> Entidade Usuario
    // ==========================================
    public Usuario toEntity(UsuarioCreationRequest request) {
        if (request == null) return null;

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setSobrenome(request.sobrenome());
        novoUsuario.setEmail(request.email());
        novoUsuario.setCpf(request.cpf());
        novoUsuario.setDataNascimento(request.dataNascimento());
        novoUsuario.setTelefone(request.telefone());
        novoUsuario.setUsername(request.username());
        novoUsuario.setStatus(Status.ATIVO);
        novoUsuario.setDataCriacao(OffsetDateTime.now());

        if (request.enderecos() != null && !request.enderecos().isEmpty()) {
            List<Endereco> listaEnderecos = request.enderecos().stream()
                    .map(dto -> {
                        Endereco novoEndereco = new Endereco();
                        novoEndereco.setPais(dto.pais());
                        novoEndereco.setEstado(dto.estado());
                        novoEndereco.setCidade(dto.cidade());
                        novoEndereco.setBairro(dto.bairro());
                        novoEndereco.setRua(dto.rua());
                        novoEndereco.setNumero(dto.numero());
                        novoEndereco.setComplemento(dto.complemento());
                        novoEndereco.setCep(dto.cep());
                        novoEndereco.setUsuario(novoUsuario);
                        return novoEndereco;
                    }).toList();

            novoUsuario.setEnderecos(listaEnderecos);
        }
        return novoUsuario;
    }
}