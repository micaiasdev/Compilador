package br.ufpi.jss.tipos;

import java.util.Objects;

/**
 * Tipo de um objeto, definido por uma declaração de classe.
 *
 * <p>Representa apenas a <b>identidade</b> do tipo (o nome da classe). A estrutura
 * detalhada — atributos, métodos e construtor — vive em
 * {@code br.ufpi.jss.simbolos.SimboloClasse}, mantida na tabela de símbolos.
 * Assim a camada {@code tipos} não depende de {@code simbolos}.</p>
 */
public final class TipoClasse implements Tipo {

    private final String nome;

    public TipoClasse(String nome) {
        this.nome = Objects.requireNonNull(nome, "nome");
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TipoClasse)) {
            return false;
        }
        return nome.equals(((TipoClasse) o).nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}
