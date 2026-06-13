package com.br.infnet.userservice.repository;

import com.br.infnet.userservice.domain.Usuario;
import com.br.infnet.userservice.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    @Query("SELECT u " +
            "FROM Usuario u " +
            "WHERE u.status = 'SUSPENSO' " +
            "AND u.reputacao.suspensoAte < CURRENT_TIMESTAMP")
    List<Usuario> findSuspendedUsersWithExpiredSuspension();

    @Query("SELECT u " +
            "FROM Usuario u " +
            "WHERE u.status = 'SUSPENSO' ")
    List<Usuario> findSuspendedUsers(Status status);
}
