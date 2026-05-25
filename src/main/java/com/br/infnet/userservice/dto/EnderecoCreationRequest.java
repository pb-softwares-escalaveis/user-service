package com.br.infnet.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoCreationRequest(
        @NotBlank(message = "O país não pode ficar em branco")
        @Size(max = 100)
        String pais,

        @NotBlank(message = "O estado não pode ficar em branco")
        @Size(max = 100)
        String estado,

        @NotBlank(message = "A cidade não pode ficar em branco")
        @Size(max = 150)
        String cidade,

        @NotBlank(message = "O bairro não pode ficar em branco")
        @Size(max = 150)
        String bairro,

        @NotBlank(message = "A rua é obrigatória")
        @Size(max = 255)
        String rua,

        @Size(max = 10)
        String numero,

        @Size(max = 50)
        String complemento,

        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "Formato de CEP suportado apenas com ou sem traço: XXXXX-XXX")
        String cep
) {
}