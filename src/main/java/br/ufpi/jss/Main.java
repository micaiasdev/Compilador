package br.ufpi.jss;

/**
 * Ponto de entrada do compilador da linguagem Java Script Simplificado (JSS).
 *
 * <p>Fase 0: esqueleto mínimo apenas para validar a cadeia de build (Maven +
 * antlr4-maven-plugin + maven-shade-plugin) e a execução do fat jar. As fases
 * seguintes substituem este corpo pelo pipeline real do front-end:
 * léxico &rarr; sintaxe &rarr; análise semântica, lendo o código-fonte da
 * entrada padrão.</p>
 */
public final class Main {

    private Main() {
        // Classe utilitária: não deve ser instanciada.
    }

    public static void main(String[] args) {
        System.out.println("JSS front-end - esqueleto (fase 0)");
    }
}
