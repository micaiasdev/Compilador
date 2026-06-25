package br.ufpi.jss.simbolos;

import br.ufpi.jss.tipos.Tipo;

/**
 * Entrada da tabela de símbolos: um identificador declarado (variável,
 * constante, parâmetro, função ou classe) com seu tipo e metadados.
 *
 * <p>Funções e classes são representadas pelas subclasses {@link SimboloFuncao}
 * e {@link SimboloClasse}, mas continuam sendo {@code Simbolo} para que
 * compartilhem o mesmo espaço de nomes — assim o nome de uma função não pode
 * colidir com o de uma variável/constante no mesmo escopo.</p>
 */
public class Simbolo {

    private final String nome;
    private final Tipo tipo;
    private final boolean constante;
    private final int linha;
    private boolean inicializado;

    public Simbolo(String nome, Tipo tipo, boolean constante, boolean inicializado, int linha) {
        this.nome = nome;
        this.tipo = tipo;
        this.constante = constante;
        this.inicializado = inicializado;
        this.linha = linha;
    }

    public String getNome() {
        return nome;
    }

    public Tipo getTipo() {
        return tipo;
    }

    /** {@code true} se declarado com {@code const} (não pode ser reatribuído). */
    public boolean isConstante() {
        return constante;
    }

    /** {@code true} se for uma variável mutável (oposto de constante). */
    public boolean isMutavel() {
        return !constante;
    }

    public boolean isInicializado() {
        return inicializado;
    }

    public void setInicializado(boolean inicializado) {
        this.inicializado = inicializado;
    }

    /** Linha da declaração no código-fonte. */
    public int getLinha() {
        return linha;
    }

    @Override
    public String toString() {
        return nome + ": " + (tipo == null ? "?" : tipo.nome());
    }
}
