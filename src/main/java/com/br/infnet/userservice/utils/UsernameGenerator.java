package com.br.infnet.userservice.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UsernameGenerator {
    private static final String[] TEMAS = {
            "Licitante", "Arrematante", "Colecionador", "Martelo",
            "Lance", "Ouro", "Prata", "Raro", "Certeiro", "Invicto", "Bidder",
            "Vencedor", "Leiloeiro", "Exclusivo", "Prime", "Elite",
            "Auctioneer", "Gavel", "Top"
    };

    public static List<String> gerarSugestoes(String nomeUsuario, int quantidade) {
        List<String> sugestoes = new ArrayList<>();
        Random random = new Random();
        String base = nomeUsuario.trim().toLowerCase().replaceAll("[^a-z0-9]", "");

        while (sugestoes.size() < quantidade) {
            String temaEscolhido = TEMAS[random.nextInt(TEMAS.length)];
            int numeroAleatorio = random.nextInt(999) + 1;

            String sugestao = "";
            int tipoFormato = random.nextInt(3);

            switch (tipoFormato) {
                case 0 -> sugestao = base + temaEscolhido.toLowerCase() + numeroAleatorio;
                case 1 -> sugestao = temaEscolhido.toLowerCase() + "_" + base + random.nextInt(99);
                case 2 -> sugestao = base + numeroAleatorio + temaEscolhido.toLowerCase();
            }

            //Evita sugestões repetidas na mesma lista
            if (!sugestoes.contains(sugestao)) {
                sugestoes.add(sugestao);
            }
        }
        return sugestoes;
    }
}