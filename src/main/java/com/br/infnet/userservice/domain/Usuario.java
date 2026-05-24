package com.br.infnet.userservice.domain;

import com.br.infnet.userservice.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "tb_usuario", schema = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq_gen")
    @SequenceGenerator(schema = "usuarios", name = "usuario_seq_gen", sequenceName = "usuario_seq", allocationSize = 1)
    private Long id;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "sobrenome", nullable = false)
    private String sobrenome;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Auth auth;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "telefone", nullable = false, unique = true)
    private String telefone;

    @Enumerated (EnumType.STRING)
    private Status status;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reputacao reputacao;

    @Column(name = "data_criacao", updatable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "data_ultimo_login")
    private OffsetDateTime ultimoLogin;

    @Column(name = "data_atualizacao")
    private OffsetDateTime dataAtualizacao;
}