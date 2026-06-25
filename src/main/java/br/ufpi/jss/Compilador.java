package br.ufpi.jss;

import br.ufpi.jss.erro.ErroCompilacao;
import br.ufpi.jss.erro.OuvinteErroSintatico;
import br.ufpi.jss.semantico.AnalisadorSemantico;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Fachada do front-end: executa o pipeline completo de análise de um programa
 * JSS — léxico &rarr; sintaxe &rarr; semântica — e devolve a mensagem de
 * resultado no formato exigido pela especificação.
 *
 * <p>É independente de {@code System.in}/{@code System.out} para poder ser usado
 * em testes; o {@link Main} cuida da entrada/saída padrão.</p>
 */
public final class Compilador {

    /** Mensagem de sucesso exigida pela especificação. */
    public static final String SUCESSO = "Linguagem compilada com sucesso";

    /**
     * Compila o conteúdo de um {@link CharStream}.
     *
     * @return {@link #SUCESSO} ou {@code "Erro na linha X: ..."} no primeiro erro.
     */
    public String compilar(CharStream entrada) {
        try {
            JSSLexer lexer = new JSSLexer(entrada);
            lexer.removeErrorListeners();
            lexer.addErrorListener(OuvinteErroSintatico.INSTANCIA);

            JSSParser parser = new JSSParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(OuvinteErroSintatico.INSTANCIA);

            JSSParser.ProgramContext arvore = parser.program();
            new AnalisadorSemantico().visit(arvore);
            return SUCESSO;
        } catch (ErroCompilacao e) {
            return e.mensagemFormatada();
        }
    }

    /** Compila uma String (atalho conveniente para testes). */
    public String compilar(String fonte) {
        return compilar(CharStreams.fromString(fonte));
    }
}
