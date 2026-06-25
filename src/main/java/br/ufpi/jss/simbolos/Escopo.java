package br.ufpi.jss.simbolos;

import java.util.HashMap;
import java.util.Map;

/**
 * Um nível de escopo: mapeia nomes para símbolos e referencia o escopo pai.
 *
 * <p>O escopo global tem pai {@code null}. Escopos de bloco (funções, if, while,
 * for, blocos {@code { }}) encadeiam-se pelo pai, permitindo busca do mais
 * interno ao mais externo e <i>shadowing</i> em níveis internos.</p>
 */
public class Escopo {

    private final Escopo pai;
    private final Map<String, Simbolo> simbolos = new HashMap<>();

    public Escopo(Escopo pai) {
        this.pai = pai;
    }

    public Escopo getPai() {
        return pai;
    }

    public boolean isGlobal() {
        return pai == null;
    }

    /** {@code true} se o nome já foi declarado <b>neste</b> escopo. */
    public boolean declaradoLocalmente(String nome) {
        return simbolos.containsKey(nome);
    }

    public void declarar(Simbolo simbolo) {
        simbolos.put(simbolo.getNome(), simbolo);
    }

    /** Busca apenas neste escopo; {@code null} se não houver. */
    public Simbolo buscarLocal(String nome) {
        return simbolos.get(nome);
    }
}
