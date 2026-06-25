package br.ufpi.jss.erro;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Ouvinte de erros léxicos e sintáticos do ANTLR que <b>interrompe a
 * compilação no primeiro erro</b>.
 *
 * <p>Ao detectar o primeiro erro, o ANTLR chama {@link #syntaxError} (tanto no
 * lexer quanto no parser, usando a {@code DefaultErrorStrategy}); aqui isso é
 * convertido imediatamente em uma {@link ErroCompilacao}, que sobe pela pilha e
 * encerra a análise. As mensagens são traduzidas para português.</p>
 *
 * <p>Observação: <b>não</b> se usa {@code BailErrorStrategy}, pois ela lança
 * {@code ParseCancellationException} dentro de {@code recoverInline} antes que o
 * parser chame este ouvinte — o que ignoraria nossa mensagem e a linha. O
 * próprio lançamento de {@link ErroCompilacao} a partir daqui já garante a
 * parada no primeiro erro.</p>
 */
public final class OuvinteErroSintatico extends org.antlr.v4.runtime.BaseErrorListener {

    /** Instância única (o ouvinte é sem estado). */
    public static final OuvinteErroSintatico INSTANCIA = new OuvinteErroSintatico();

    private OuvinteErroSintatico() {
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        throw new ErroCompilacao(line, descrever(recognizer, offendingSymbol, charPositionInLine, e));
    }

    /** Monta uma descrição em português a partir do contexto do erro. */
    private String descrever(Recognizer<?, ?> recognizer,
                             Object offendingSymbol,
                             int charPositionInLine,
                             RecognitionException e) {
        if (recognizer instanceof Lexer) {
            return "símbolo não reconhecido na coluna " + (charPositionInLine + 1);
        }

        Token token = (offendingSymbol instanceof Token) ? (Token) offendingSymbol : null;
        String descricao;
        if (token == null) {
            descricao = "entrada inválida";
        } else if (token.getType() == Token.EOF) {
            descricao = "fim de arquivo inesperado";
        } else {
            descricao = "token inesperado '" + token.getText() + "'";
        }

        if (e != null && recognizer instanceof Parser) {
            IntervalSet esperados = e.getExpectedTokens();
            if (esperados != null && !esperados.isNil()) {
                descricao += " (esperado: " + esperados.toString(recognizer.getVocabulary()) + ")";
            }
        }
        return descricao;
    }
}
