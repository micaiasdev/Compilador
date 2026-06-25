package br.ufpi.jss.erro;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/** Listener que converte erros lexicos/sintaticos do ANTLR em diagnosticos JSS. */
public final class OuvinteErroSintatico extends org.antlr.v4.runtime.BaseErrorListener {

    private final ColetorErros erros;

    public OuvinteErroSintatico(ColetorErros erros) {
        this.erros = erros;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        erros.adicionar(line, descrever(recognizer, offendingSymbol, charPositionInLine, e));
    }

    /** Monta uma descricao em portugues a partir do contexto do erro. */
    private String descrever(Recognizer<?, ?> recognizer,
                             Object offendingSymbol,
                             int charPositionInLine,
                             RecognitionException e) {
        if (recognizer instanceof Lexer) {
            return "simbolo nao reconhecido na coluna " + (charPositionInLine + 1);
        }

        Token token = (offendingSymbol instanceof Token) ? (Token) offendingSymbol : null;
        String descricao;
        if (token == null) {
            descricao = "entrada invalida";
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
