package com.br.infnet.userservice.domain;

import com.br.infnet.userservice.enums.ModoVerificacao;
import com.br.infnet.userservice.enums.Role;
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

    @Column(name = "hash_senha", nullable = false, unique = true)
    private String hashSenha;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "is_verificado", nullable = false)
    private boolean isVerificado;

    @Enumerated(EnumType.STRING)
    private ModoVerificacao modoVerificacao;

    @Column(name="verificacao_duas_etapas", nullable = false)
    private boolean verificacaoDuasEtapas;
}
