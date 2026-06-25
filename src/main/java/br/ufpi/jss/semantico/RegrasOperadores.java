package br.ufpi.jss.semantico;

import br.ufpi.jss.erro.ColetorErros;
import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.TipoErro;
import br.ufpi.jss.tipos.Tipos;

import static br.ufpi.jss.tipos.TipoPrimitivo.BOOL;
import static br.ufpi.jss.tipos.TipoPrimitivo.INT;
import static br.ufpi.jss.tipos.TipoPrimitivo.STR;

/** Regras de tipagem dos operadores aritmeticos, relacionais e logicos do JSS. */
public final class RegrasOperadores {

    private RegrasOperadores() {
    }

    /** Aplica um operador binario e devolve o tipo do resultado. */
    public static Tipo binario(String op, Tipo a, Tipo b, int linha, ColetorErros erros) {
        if (Tipos.ehErro(a) || Tipos.ehErro(b)) {
            return TipoErro.INSTANCIA;
        }

        switch (op) {
            case "+":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return Tipos.promoverNumerico(a, b);
                }
                if (a == STR && b == STR) {
                    return STR;
                }
                return incompativel(erros, linha, "+", a, b);

            case "-":
            case "*":
            case "/":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return Tipos.promoverNumerico(a, b);
                }
                return incompativel(erros, linha, op, a, b);

            case "%":
                if (a == INT && b == INT) {
                    return INT;
                }
                erros.adicionar(linha, "operador '%' requer dois inteiros (int % int)");
                return TipoErro.INSTANCIA;

            case "**":
                if (a == INT && b == INT) {
                    return INT;
                }
                erros.adicionar(linha, "operador '**' requer dois inteiros (int ** int)");
                return TipoErro.INSTANCIA;

            case "<":
            case ">":
            case "<=":
            case ">=":
                if (Tipos.ehNumerico(a) && Tipos.ehNumerico(b)) {
                    return BOOL;
                }
                erros.adicionar(linha,
                    "operador relacional '" + op + "' requer operandos numericos (int ou real)");
                return TipoErro.INSTANCIA;

            case "==":
            case "!=":
                if (Tipos.comparavelIgualdade(a, b)) {
                    return BOOL;
                }
                erros.adicionar(linha,
                    "operador '" + op + "' requer operandos de tipos compativeis ("
                        + a.nome() + " e " + b.nome() + ")");
                return TipoErro.INSTANCIA;

            case "&&":
            case "||":
                if (a == BOOL && b == BOOL) {
                    return BOOL;
                }
                erros.adicionar(linha, "operador logico '" + op + "' requer operandos bool");
                return TipoErro.INSTANCIA;

            default:
                erros.adicionar(linha, "operador binario desconhecido '" + op + "'");
                return TipoErro.INSTANCIA;
        }
    }

    /** Aplica um operador unario e devolve o tipo do resultado. */
    public static Tipo unario(String op, Tipo t, int linha, ColetorErros erros) {
        if (Tipos.ehErro(t)) {
            return TipoErro.INSTANCIA;
        }

        switch (op) {
            case "!":
                if (t == BOOL) {
                    return BOOL;
                }
                erros.adicionar(linha, "operador '!' requer operando bool (encontrado " + t.nome() + ")");
                return TipoErro.INSTANCIA;

            case "+":
            case "-":
                if (Tipos.ehNumerico(t)) {
                    return t;
                }
                erros.adicionar(linha,
                    "operador unario '" + op + "' requer operando numerico (encontrado " + t.nome() + ")");
                return TipoErro.INSTANCIA;

            case "++":
            case "--":
                if (Tipos.ehNumerico(t)) {
                    return t;
                }
                erros.adicionar(linha,
                    "operador '" + op + "' requer operando numerico (encontrado " + t.nome() + ")");
                return TipoErro.INSTANCIA;

            default:
                erros.adicionar(linha, "operador unario desconhecido '" + op + "'");
                return TipoErro.INSTANCIA;
        }
    }

    private static Tipo incompativel(ColetorErros erros, int linha, String op, Tipo a, Tipo b) {
        erros.adicionar(linha,
            "operador '" + op + "' nao pode ser aplicado a " + a.nome() + " e " + b.nome());
        return TipoErro.INSTANCIA;
    }
}
