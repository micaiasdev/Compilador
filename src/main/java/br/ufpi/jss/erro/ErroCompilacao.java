package br.ufpi.jss.erro;

/**
 * Exceção que representa qualquer erro de compilação do front-end JSS:
 * léxico, sintático ou semântico.
 *
 * <p>Carrega a linha do código-fonte onde o erro ocorreu, de modo que a saída
 * possa seguir o formato exigido pela especificação:
 * {@code Erro na linha X: descrição do erro}.</p>
 *
 * <p>Estende {@link RuntimeException} (não checada) porque é lançada de dentro
 * de métodos do ANTLR cuja assinatura não declara exceções checadas
 * (ex.: {@code ANTLRErrorListener.syntaxError}) e de dentro do visitor de
 * análise semântica.</p>
 */
public class ErroCompilacao extends RuntimeException {

    private final int linha;

    public ErroCompilacao(int linha, String descricao) {
        super(descricao);
        this.linha = linha;
    }

    /** Linha do código-fonte onde o erro foi detectado. */
    public int getLinha() {
        return linha;
    }

    /** Descrição do erro (sem o prefixo de linha). */
    public String getDescricao() {
        return getMessage();
    }

    /**
     * Mensagem no formato exigido pela especificação.
     *
     * @return {@code "Erro na linha X: descrição"}
     */
    public String mensagemFormatada() {
        return "Erro na linha " + linha + ": " + getMessage();
    }
}
