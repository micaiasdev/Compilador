package br.ufpi.jss.simbolos;

import br.ufpi.jss.erro.ErroCompilacao;

/**
 * Pilha de escopos da análise semântica.
 *
 * <p>Implementa as regras de escopo do JSS:</p>
 * <ul>
 *   <li>{@link #declarar} verifica apenas o escopo atual — redeclarar um nome no
 *       mesmo escopo é erro;</li>
 *   <li>{@link #resolver} busca do escopo mais interno ao mais externo, então
 *       <i>shadowing</i> em escopos internos é permitido.</li>
 * </ul>
 */
public class TabelaDeSimbolos {

    private Escopo atual;

    public TabelaDeSimbolos() {
        this.atual = new Escopo(null); // escopo global
    }

    /** Abre um novo escopo de bloco (filho do atual). */
    public void entrarEscopo() {
        atual = new Escopo(atual);
    }

    /** Fecha o escopo atual, voltando ao pai (nunca remove o global). */
    public void sairEscopo() {
        if (atual.getPai() != null) {
            atual = atual.getPai();
        }
    }

    public boolean noEscopoGlobal() {
        return atual.isGlobal();
    }

    /**
     * Declara um símbolo no escopo atual.
     *
     * @throws ErroCompilacao se já existir um identificador com esse nome neste
     *                        mesmo escopo (redeclaração).
     */
    public void declarar(Simbolo simbolo) {
        if (atual.declaradoLocalmente(simbolo.getNome())) {
            throw new ErroCompilacao(
                simbolo.getLinha(),
                "identificador '" + simbolo.getNome() + "' já declarado neste escopo");
        }
        atual.declarar(simbolo);
    }

    /**
     * Resolve um nome buscando do escopo atual para os externos.
     *
     * @return o símbolo encontrado, ou {@code null} se não declarado.
     */
    public Simbolo resolver(String nome) {
        for (Escopo escopo = atual; escopo != null; escopo = escopo.getPai()) {
            Simbolo s = escopo.buscarLocal(nome);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /** {@code true} se o nome já existe no escopo atual (para checagem prévia). */
    public boolean declaradoNoEscopoAtual(String nome) {
        return atual.declaradoLocalmente(nome);
    }
}
