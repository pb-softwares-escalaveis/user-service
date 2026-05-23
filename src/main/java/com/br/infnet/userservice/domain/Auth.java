package com.br.infnet.userservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_auth", schema = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Auth {
    @Id
    @GeneratedValue(generator = "auth_seq_gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "auth_seq_gen", sequenceName = "auth_seq", schema = "usuarios", allocationSize = 1)
    private Long id;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String hashSenha;

    private String token;

    private boolean verificado;

    private boolean verificacao2etapas;
}
