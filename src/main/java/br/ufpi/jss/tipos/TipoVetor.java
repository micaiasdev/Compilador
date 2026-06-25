package br.ufpi.jss.tipos;

import java.util.Objects;

/**
 * Tipo vetor. Vetores multidimensionais são representados por aninhamento:
 * {@code int[3][4]} é {@code TipoVetor(TipoVetor(int, 4), 3)}.
 *
 * <p>Cada nível guarda o tamanho daquela dimensão ({@link #getDimensao()}),
 * usado para validar o comprimento de literais na declaração. O tamanho pode ser
 * {@link #DIMENSAO_NAO_ESPECIFICADA} quando omitido (ex.: parâmetro {@code int[]}).</p>
 *
 * <p>A <b>identidade de tipo</b> ({@code equals}) considera apenas o tipo-base e o
 * número de dimensões — não os tamanhos —, pois {@code int[3]} e {@code int[4]}
 * são o mesmo tipo para fins de compatibilidade.</p>
 */
public final class TipoVetor implements Tipo {

    public static final int DIMENSAO_NAO_ESPECIFICADA = -1;

    private final Tipo elemento;
    private final int dimensao;

    public TipoVetor(Tipo elemento, int dimensao) {
        this.elemento = Objects.requireNonNull(elemento, "elemento");
        this.dimensao = dimensao;
    }

    /** Tipo do elemento desta dimensão (pode ser outro {@link TipoVetor}). */
    public Tipo getElemento() {
        return elemento;
    }

    /** Tamanho declarado desta dimensão, ou {@link #DIMENSAO_NAO_ESPECIFICADA}. */
    public int getDimensao() {
        return dimensao;
    }

    /** Quantidade total de dimensões (1 para vetor simples). */
    public int numDimensoes() {
        return (elemento instanceof TipoVetor) ? 1 + ((TipoVetor) elemento).numDimensoes() : 1;
    }

    /** Tipo escalar na base do vetor (o elemento mais interno, não-vetor). */
    public Tipo tipoBase() {
        return (elemento instanceof TipoVetor) ? ((TipoVetor) elemento).tipoBase() : elemento;
    }

    @Override
    public String nome() {
        return tipoBase().nome() + "[]".repeat(numDimensoes());
    }

    @Override
    public String toString() {
        return nome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TipoVetor)) {
            return false;
        }
        TipoVetor outro = (TipoVetor) o;
        return numDimensoes() == outro.numDimensoes() && tipoBase().equals(outro.tipoBase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoBase(), numDimensoes());
    }
}
