package com.br.infnet.userservice.domain;

import com.br.infnet.userservice.enums.Status;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_usuario", schema = "usuarios", check = {
        @CheckConstraint(name = "chk_idade_minima", constraint = "data_nascimento <= (CURRENT_DATE - interval '18 years')"),
        @CheckConstraint(name = "chk_username_length", constraint = "char_length(username) >= 4 AND char_length(username) <= 50"),
        @CheckConstraint(name = "chk_nome_length", constraint = "char_length(nome) >= 2 AND char_length(nome) <= 50"),
        @CheckConstraint(name = "chk_sobrenome_length", constraint = "char_length(sobrenome) >= 2 AND char_length(sobrenome) <= 50"),
        @CheckConstraint(name = "chk_email_format", constraint = "email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'"),
        @CheckConstraint(name = "chk_telefone_format", constraint = "telefone ~ '^\\d{10,11}$'")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "id", unique = true, length = 36)
    private UUID id;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos;

    @Column(name = "nome", nullable = false, updatable = false)
    private String nome;

    @Column(name = "sobrenome", nullable = false, updatable = false)
    private String sobrenome;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cpf", nullable = false, unique = true, updatable = false)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false, updatable = false)
    private LocalDate dataNascimento;

    @Column(name = "telefone", nullable = false, unique = true)
    private String telefone;

    @Enumerated (EnumType.STRING)
    private Status status;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reputacao reputacao;

    @Column(name = "data_criacao", updatable = false)
    private Instant dataCriacao;

    @Column(name = "data_ultimo_login")
    private Instant ultimoLogin;

    @Column(name = "data_atualizacao")
    private Instant dataAtualizacao;
}