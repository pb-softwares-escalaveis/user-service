package com.br.infnet.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.LinkedList;

public record UsuarioCreationRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
        String nome,

        @NotBlank(message = "O sobrenome não pode estar em branco")
        @Size(min = 2, max = 50, message = "O sobrenome deve ter entre 2 e 50 caracteres")
        String sobrenome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "^\\d{11}$", message = "O CPF deve conter exatamente 11 dígitos numéricos")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @NotBlank
        @Pattern(regexp = "^\\d{10,11}$", message = "O telefone deve ter formato ddd + numérico válido")
        String telefone,

        @NotBlank
        @Size(min = 4, max = 50, message = "O username deve conter de 4 até 50 caracteres")
        String username,

        @NotBlank
        @Size(min = 8, message = "A senha deve conter ao menos 8 caracteres")
        String senha,

        @Valid
        LinkedList<EnderecoCreationRequest> enderecos
) {
}
