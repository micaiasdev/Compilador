package br.ufpi.jss.tipos;

/**
 * Utilitários do sistema de tipos: predicados e regras de compatibilidade
 * reutilizados pela análise semântica.
 */
public final class Tipos {

    private Tipos() {
        // Classe utilitária.
    }

    /** {@code true} se o tipo for numérico (int ou real). */
    public static boolean ehNumerico(Tipo t) {
        return t == TipoPrimitivo.INT || t == TipoPrimitivo.REAL;
    }

    public static boolean ehErro(Tipo t) {
        return t == TipoErro.INSTANCIA;
    }

    /** {@code true} se o tipo for um primitivo de valor (int, real, str, bool). */
    public static boolean ehPrimitivo(Tipo t) {
        return t == TipoPrimitivo.INT
            || t == TipoPrimitivo.REAL
            || t == TipoPrimitivo.STR
            || t == TipoPrimitivo.BOOL;
    }

    /**
     * Promoção numérica de uma operação binária: {@code real} se algum operando
     * for real, senão {@code int}. Retorna {@code null} se algum não for numérico.
     */
    public static Tipo promoverNumerico(Tipo a, Tipo b) {
        if (!ehNumerico(a) || !ehNumerico(b)) {
            return null;
        }
        return (a == TipoPrimitivo.REAL || b == TipoPrimitivo.REAL)
            ? TipoPrimitivo.REAL
            : TipoPrimitivo.INT;
    }

    /**
     * Compatibilidade de atribuição: o valor de tipo {@code origem} pode ser
     * atribuído a um destino de tipo {@code destino}?
     *
     * <ul>
     *   <li>tipos iguais — sim;</li>
     *   <li>{@code int} &rarr; {@code real} — sim (promoção implícita);</li>
     *   <li>{@code real} &rarr; {@code int} — não (exige cast explícito);</li>
     *   <li>{@code null} &rarr; objeto (classe) — sim.</li>
     * </ul>
     */
    public static boolean compativelAtribuicao(Tipo destino, Tipo origem) {
        if (ehErro(destino) || ehErro(origem)) {
            return true;
        }
        if (destino.equals(origem)) {
            return true;
        }
        if (destino == TipoPrimitivo.REAL && origem == TipoPrimitivo.INT) {
            return true;
        }
        return origem == TipoPrimitivo.NULL && destino instanceof TipoClasse;
    }

    /**
     * Operandos comparáveis por {@code ==} / {@code !=}: mesmo tipo, ou um numérico
     * com outro numérico (int &harr; real, com promoção implícita).
     */
    public static boolean comparavelIgualdade(Tipo a, Tipo b) {
        if (ehErro(a) || ehErro(b)) {
            return true;
        }
        if (a.equals(b)) {
            return true;
        }
        return ehNumerico(a) && ehNumerico(b);
    }
}
