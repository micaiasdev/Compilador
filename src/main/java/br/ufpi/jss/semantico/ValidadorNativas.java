package br.ufpi.jss.semantico;

import br.ufpi.jss.erro.ErroCompilacao;
import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.TipoPrimitivo;
import br.ufpi.jss.tipos.Tipos;

import static br.ufpi.jss.tipos.TipoPrimitivo.REAL;
import static br.ufpi.jss.tipos.TipoPrimitivo.STR;

/**
 * Regras de tipo das funções nativas {@code input}, {@code console.log} e das
 * conversões explícitas ({@code int}, {@code real}, {@code bool}, {@code str}).
 *
 * <p>As checagens estruturais (ex.: {@code input} só aceita variáveis mutáveis)
 * ficam no analisador, pois dependem da árvore; aqui ficam as regras que
 * dependem apenas dos tipos.</p>
 */
public final class ValidadorNativas {

    private ValidadorNativas() {
    }

    /** {@code console.log} aceita apenas int, real, str ou bool. */
    public static void validarTipoConsoleLog(Tipo t, int linha) {
        if (!Tipos.ehPrimitivo(t)) {
            throw new ErroCompilacao(linha,
                "console.log aceita apenas int, real, str ou bool (recebido " + t.nome() + ")");
        }
    }

    /** {@code input} aceita apenas variáveis dos tipos int, real ou str (bool não). */
    public static void validarTipoInput(Tipo t, int linha) {
        if (!Tipos.ehNumerico(t) && t != STR) {
            throw new ErroCompilacao(linha,
                "input aceita apenas variáveis int, real ou str (recebido " + t.nome() + ")");
        }
    }

    /**
     * Valida uma conversão explícita {@code destino(arg)} e devolve o tipo do
     * resultado.
     *
     * <ul>
     *   <li>qualquer primitivo pode ser convertido para {@code str};</li>
     *   <li>{@code int}, {@code real} e {@code bool} são intercambiáveis;</li>
     *   <li>{@code str} não pode ser convertido para int/real/bool.</li>
     * </ul>
     */
    public static Tipo validarCast(String destino, Tipo arg, int linha) {
        TipoPrimitivo alvo = TipoPrimitivo.porNome(destino);
        if (!Tipos.ehPrimitivo(arg)) {
            throw new ErroCompilacao(linha,
                "não é possível converter " + arg.nome() + " para " + destino);
        }
        if (alvo == STR) {
            return STR; // qualquer primitivo -> str
        }
        if (arg == STR) {
            throw new ErroCompilacao(linha, "não é possível converter str para " + destino);
        }
        return alvo; // int/real/bool intercambiáveis
    }
}
