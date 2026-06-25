package br.ufpi.jss.semantico;

import br.ufpi.jss.erro.ErroCompilacao;
import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.Tipos;

import static br.ufpi.jss.tipos.TipoPrimitivo.BOOL;
import static br.ufpi.jss.tipos.TipoPrimitivo.INT;
import static br.ufpi.jss.tipos.TipoPrimitivo.STR;

/**
 * Regras de tipagem dos operadores aritméticos, relacionais e lógicos do JSS,
 * conforme a especificação (§4.3) e as decisões do grupo.
 *
 * <p>Cada método retorna o tipo do resultado da operação ou lança
 * {@link ErroCompilacao} quando os operandos são incompatíveis. As regras de
 * <i>lvalue</i>/mutabilidade de {@code ++}/{@code --} ficam no analisador, pois
 * dependem da estrutura da árvore, não apenas dos tipos.</p>
 */
public final class RegrasOperadores {

    private RegrasOperadores() {
    }

    /** Aplica um operador binário e devolve o tipo do resultado. */
    public static Tipo binario(String op, Tipo a, Tipo b, int linha) {
        switch (op) {
            case "+":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return Tipos.promoverNumerico(a, b);
                }
                if (a == STR && b == STR) {
                    return STR;
                }
                throw incompativel(linha, "+", a, b);

            case "-":
            case "*":
            case "/":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return Tipos.promoverNumerico(a, b);
                }
                throw incompativel(linha, op, a, b);

            case "%":
                if (a == INT && b == INT) {
                    return INT;
                }
                throw new ErroCompilacao(linha, "operador '%' requer dois inteiros (int % int)");

            case "**":
                if (a == INT && b == INT) {
                    return INT;
                }
                throw new ErroCompilacao(linha, "operador '**' requer dois inteiros (int ** int)");

            case "<":
            case ">":
            case "<=":
            case ">=":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return BOOL;
                }
                throw new ErroCompilacao(linha,
                    "operador relacional '" + op + "' requer operandos numéricos (int ou real)");

            case "==":
            case "!=":
                if (Tipos.comparavelIgualdade(a, b)) {
                    return BOOL;
                }
                throw new ErroCompilacao(linha,
                    "operador '" + op + "' requer operandos de tipos compatíveis ("
                        + a.nome() + " e " + b.nome() + ")");

            case "&&":
            case "||":
                if (a == BOOL && b == BOOL) {
                    return BOOL;
                }
                throw new ErroCompilacao(linha, "operador lógico '" + op + "' requer operandos bool");

            default:
                throw new ErroCompilacao(linha, "operador binário desconhecido '" + op + "'");
        }
    }

    /** Aplica um operador unário e devolve o tipo do resultado. */
    public static Tipo unario(String op, Tipo t, int linha) {
        switch (op) {
            case "!":
                if (t == BOOL) {
                    return BOOL;
                }
                throw new ErroCompilacao(linha, "operador '!' requer operando bool (encontrado " + t.nome() + ")");

            case "+":
            case "-":
                if (Tipos.ehNumerico(t)) {
                    return t;
                }
                throw new ErroCompilacao(linha,
                    "operador unário '" + op + "' requer operando numérico (encontrado " + t.nome() + ")");

            case "++":
            case "--":
                if (Tipos.ehNumerico(t)) {
                    return t;
                }
                throw new ErroCompilacao(linha,
                    "operador '" + op + "' requer operando numérico (encontrado " + t.nome() + ")");

            default:
                throw new ErroCompilacao(linha, "operador unário desconhecido '" + op + "'");
        }
    }

    private static ErroCompilacao incompativel(int linha, String op, Tipo a, Tipo b) {
        return new ErroCompilacao(linha,
            "operador '" + op + "' não pode ser aplicado a " + a.nome() + " e " + b.nome());
    }
}
