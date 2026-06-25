package br.ufpi.jss.tipos;

/**
 * Tipo de um valor na linguagem JSS.
 *
 * <p>Base do sistema de tipos. Implementado por:</p>
 * <ul>
 *   <li>{@link TipoPrimitivo} — int, real, str, bool, void, null;</li>
 *   <li>{@link TipoVetor} — vetores (aninháveis para múltiplas dimensões);</li>
 *   <li>{@link TipoClasse} — tipos definidos por declarações de classe.</li>
 * </ul>
 *
 * <p>É a camada mais baixa do compilador: não depende de símbolos nem do ANTLR.</p>
 */
public interface Tipo {

    /** Representação textual do tipo, usada em mensagens de erro (ex.: {@code "int[][]"}). */
    String nome();
}
