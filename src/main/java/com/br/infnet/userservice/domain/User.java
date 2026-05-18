package com.br.infnet.userservice.domain;

import com.br.infnet.userservice.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_users", schema = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq_gen")
    @SequenceGenerator(schema = "usuario", name = "usuario_seq_gen", sequenceName = "usuario_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String cpf;

    @Column(nullable = false)
    private int marks;

    @Enumerated (EnumType.STRING)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}