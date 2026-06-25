package br.ufpi.jss.codegen;

import br.ufpi.jss.JSSBaseVisitor;
import br.ufpi.jss.JSSParser.ProgramContext;
import br.ufpi.jss.semantico.InfoSemantica;

/**
 * Esqueleto do gerador de código intermediário <b>Jasmin</b> (etapa de back-end).
 *
 * <p><b>NÃO IMPLEMENTADO NESTA ENTREGA</b> — esta classe existe apenas como ponto
 * de extensão, mostrando como o back-end se acopla ao front-end já pronto.</p>
 *
 * <h2>Como o back-end se conecta</h2>
 * <ol>
 *   <li>O front-end ({@code Compilador}) faz léxico + sintaxe + semântica e produz
 *       a {@link InfoSemantica} (árvore anotada com tipos + funções + classes).</li>
 *   <li>Este gerador percorre a mesma árvore ({@code JSSBaseVisitor<Void>}),
 *       reutilizando {@link InfoSemantica#tipoDe} e as tabelas de funções/classes
 *       para emitir instruções da JVM.</li>
 *   <li>A saída é um arquivo Jasmin {@code .j}, montado com
 *       {@code java -jar jasmin.jar Programa.j} &rarr; {@code Programa.class},
 *       executável com {@code java Programa}.</li>
 * </ol>
 *
 * <h2>Mapeamento previsto (resumo)</h2>
 * <ul>
 *   <li>tipos: {@code int}&rarr;{@code I}, {@code real}&rarr;{@code D},
 *       {@code bool}&rarr;{@code Z}, {@code str}&rarr;{@code Ljava/lang/String;},
 *       vetores&rarr;{@code [...}, classes&rarr;{@code L<nome>;};</li>
 *   <li>funções &rarr; métodos {@code static}; classes &rarr; {@code .class} com
 *       campos, construtor {@code <init>} e métodos de instância;</li>
 *   <li>{@code console.log}/{@code input} &rarr; chamadas a {@code System.out}/
 *       {@code java.util.Scanner}.</li>
 * </ul>
 */
public final class GeradorJasmin extends JSSBaseVisitor<Void> {

    private final InfoSemantica info;

    public GeradorJasmin(InfoSemantica info) {
        this.info = info;
    }

    /**
     * Gera o código Jasmin do programa. A implementação real será feita na etapa
     * de back-end, percorrendo a árvore e consultando {@link #info}.
     *
     * @throws UnsupportedOperationException sempre (ainda não implementado)
     */
    public String gerar(ProgramContext programa) {
        // TODO (back-end): emitir o arquivo .j a partir da árvore anotada em 'info'.
        throw new UnsupportedOperationException(
            "Geração de código Jasmin é a etapa de back-end (a ser implementada).");
    }
}
