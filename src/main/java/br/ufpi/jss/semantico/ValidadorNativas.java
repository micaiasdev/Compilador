package br.ufpi.jss.semantico;

import br.ufpi.jss.erro.ColetorErros;
import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.TipoErro;
import br.ufpi.jss.tipos.TipoPrimitivo;
import br.ufpi.jss.tipos.Tipos;

import static br.ufpi.jss.tipos.TipoPrimitivo.STR;

/** Regras de tipo das funcoes nativas e conversoes explicitas do JSS. */
public final class ValidadorNativas {

    private ValidadorNativas() {
    }

    /** {@code console.log} aceita apenas int, real, str ou bool. */
    public static void validarTipoConsoleLog(Tipo t, int linha, ColetorErros erros) {
        if (Tipos.ehErro(t)) {
            return;
        }
        if (!Tipos.ehPrimitivo(t)) {
            erros.adicionar(linha,
                "console.log aceita apenas int, real, str ou bool (recebido " + t.nome() + ")");
        }
    }

    /** {@code input} aceita apenas variaveis dos tipos int, real ou str. */
    public static void validarTipoInput(Tipo t, int linha, ColetorErros erros) {
        if (Tipos.ehErro(t)) {
            return;
        }
        if (!Tipos.ehNumerico(t) && t != STR) {
            erros.adicionar(linha,
                "input aceita apenas variaveis int, real ou str (recebido " + t.nome() + ")");
        }
    }

    /** Valida uma conversao explicita e devolve o tipo do resultado. */
    public static Tipo validarCast(String destino, Tipo arg, int linha, ColetorErros erros) {
        if (Tipos.ehErro(arg)) {
            return TipoErro.INSTANCIA;
        }

        TipoPrimitivo alvo = TipoPrimitivo.porNome(destino);
        if (!Tipos.ehPrimitivo(arg)) {
            erros.adicionar(linha, "nao e possivel converter " + arg.nome() + " para " + destino);
            return TipoErro.INSTANCIA;
        }
        if (alvo == STR) {
            return STR;
        }
        if (arg == STR) {
            erros.adicionar(linha, "nao e possivel converter str para " + destino);
            return TipoErro.INSTANCIA;
        }
        return alvo;
    }
}
