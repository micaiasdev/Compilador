package br.ufpi.jss.simbolos;

import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.TipoClasse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Símbolo de uma classe: guarda a estrutura usada para validar criação de
 * objetos ({@code new}), acesso a atributos e chamadas de métodos.
 *
 * <p>Atributos e métodos usam {@link LinkedHashMap} para preservar a ordem de
 * declaração (relevante para a geração de código no back-end). O tipo do símbolo
 * ({@link #getTipo()}) é o {@link TipoClasse} correspondente.</p>
 */
public class SimboloClasse extends Simbolo {

    private final Map<String, Tipo> atributos = new LinkedHashMap<>();
    private final Map<String, SimboloFuncao> metodos = new LinkedHashMap<>();
    private List<Tipo> parametrosConstrutor = new ArrayList<>();

    public SimboloClasse(String nome, int linha) {
        super(nome, new TipoClasse(nome), true, true, linha);
    }

    public TipoClasse getTipoClasse() {
        return (TipoClasse) getTipo();
    }

    // ---- Atributos ----
    public void adicionarAtributo(String nome, Tipo tipo) {
        atributos.put(nome, tipo);
    }

    public boolean temAtributo(String nome) {
        return atributos.containsKey(nome);
    }

    public Tipo tipoAtributo(String nome) {
        return atributos.get(nome);
    }

    public Map<String, Tipo> getAtributos() {
        return atributos;
    }

    // ---- Métodos ----
    public void adicionarMetodo(SimboloFuncao metodo) {
        metodos.put(metodo.getNome(), metodo);
    }

    public boolean temMetodo(String nome) {
        return metodos.containsKey(nome);
    }

    public SimboloFuncao metodo(String nome) {
        return metodos.get(nome);
    }

    // ---- Construtor ----
    public void setParametrosConstrutor(List<Tipo> parametros) {
        this.parametrosConstrutor = List.copyOf(parametros);
    }

    public List<Tipo> getParametrosConstrutor() {
        return parametrosConstrutor;
    }
}
