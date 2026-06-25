package br.ufpi.jss.erro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Acumula erros de compilacao preservando uma ordem estavel por linha. */
public final class ColetorErros {

    private final List<Item> erros = new ArrayList<>();
    private int proximaOrdem;

    public void adicionar(int linha, String descricao) {
        adicionar(new ErroCompilacao(linha, descricao));
    }

    public void adicionar(ErroCompilacao erro) {
        for (Item item : erros) {
            if (item.erro.getLinha() == erro.getLinha()
                    && item.erro.getDescricao().equals(erro.getDescricao())) {
                return;
            }
        }
        erros.add(new Item(erro, proximaOrdem++));
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }

    public List<ErroCompilacao> erros() {
        return erros.stream()
            .sorted(Comparator
                .comparingInt((Item item) -> item.erro.getLinha())
                .thenComparingInt(item -> item.ordem))
            .map(item -> item.erro)
            .toList();
    }

    public String mensagensFormatadas() {
        return String.join(System.lineSeparator(),
            erros().stream().map(ErroCompilacao::mensagemFormatada).toList());
    }

    private static final class Item {
        private final ErroCompilacao erro;
        private final int ordem;

        private Item(ErroCompilacao erro, int ordem) {
            this.erro = erro;
            this.ordem = ordem;
        }
    }
}
