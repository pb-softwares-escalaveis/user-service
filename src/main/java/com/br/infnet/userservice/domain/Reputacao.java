package com.br.infnet.userservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_reputacao", schema = "usuarios", check = {
        @CheckConstraint(name = "chk_pontos_reputacao", constraint = "reputacao >= 0 AND reputacao <= 5.0"),
        @CheckConstraint(name = "chk_marks_positivos", constraint = "marks >= 0 AND marks <= 3")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Reputacao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reputacao_seq_gen")
    @SequenceGenerator(schema = "usuarios", name = "reputacao_seq_gen", sequenceName = "reputacao_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @Column(name = "marks", nullable = false)
    private int marks;

    @Column(name = "reputacao", nullable = false)
    private float reputacao;

    @Column(name= "data_ultima_punicao")
    private OffsetDateTime dataUltimaPunicao;
}
