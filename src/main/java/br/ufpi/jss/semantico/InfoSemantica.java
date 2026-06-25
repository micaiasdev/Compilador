package br.ufpi.jss.semantico;

import br.ufpi.jss.simbolos.SimboloClasse;
import br.ufpi.jss.simbolos.SimboloFuncao;
import br.ufpi.jss.tipos.Tipo;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.Map;

/**
 * Resultado da análise semântica, projetado para ser consumido pelo back-end
 * (gerador de código intermediário Jasmin). É o "gancho" entre as duas etapas.
 *
 * <p>Reúne tudo o que o gerador de código precisa sem ter de refazer a análise:</p>
 * <ul>
 *   <li>a <b>árvore anotada</b> — o tipo resolvido de cada expressão;</li>
 *   <li>as <b>funções</b> globais (assinaturas: retorno e tipos dos parâmetros);</li>
 *   <li>as <b>classes</b> (layout de atributos, métodos e construtor).</li>
 * </ul>
 */
public final class InfoSemantica {

    private final ParseTreeProperty<Tipo> tipos;
    private final Map<String, SimboloFuncao> funcoes;
    private final Map<String, SimboloClasse> classes;

    public InfoSemantica(ParseTreeProperty<Tipo> tipos,
                         Map<String, SimboloFuncao> funcoes,
                         Map<String, SimboloClasse> classes) {
        this.tipos = tipos;
        this.funcoes = funcoes;
        this.classes = classes;
    }

    /** Tipo resolvido de uma expressão, ou {@code null} se o nó não for expressão. */
    public Tipo tipoDe(ParseTree no) {
        return tipos.get(no);
    }

    public SimboloFuncao funcao(String nome) {
        return funcoes.get(nome);
    }

    public Map<String, SimboloFuncao> getFuncoes() {
        return funcoes;
    }

    public SimboloClasse classe(String nome) {
        return classes.get(nome);
    }

    public Map<String, SimboloClasse> getClasses() {
        return classes;
    }
}
