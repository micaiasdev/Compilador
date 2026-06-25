package br.ufpi.jss.simbolos;

import br.ufpi.jss.tipos.Tipo;

import java.util.List;

/**
 * Símbolo de uma função (ou método de classe): nome, tipo de retorno e os tipos
 * dos parâmetros, usados para validar chamadas (aridade e tipos dos argumentos).
 *
 * <p>O tipo do símbolo ({@link #getTipo()}) é o tipo de retorno. Funções são
 * tratadas como constantes (o nome não pode ser reatribuído).</p>
 */
public class SimboloFuncao extends Simbolo {

    private final Tipo retorno;
    private final List<Tipo> parametros;

    public SimboloFuncao(String nome, Tipo retorno, List<Tipo> parametros, int linha) {
        super(nome, retorno, true, true, linha);
        this.retorno = retorno;
        this.parametros = List.copyOf(parametros);
    }

    public Tipo getRetorno() {
        return retorno;
    }

    /** Tipos dos parâmetros, na ordem de declaração (lista imutável). */
    public List<Tipo> getParametros() {
        return parametros;
    }

    public int aridade() {
        return parametros.size();
    }
}
