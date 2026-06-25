package br.ufpi.jss.tipos;

/**
 * Tipos primitivos e especiais da linguagem JSS.
 *
 * <p>{@code INT}, {@code REAL}, {@code STR} e {@code BOOL} são os primitivos
 * propriamente ditos. {@code VOID} é o "tipo" de retorno de funções sem valor e
 * {@code NULL} é o tipo do literal {@code null} (compatível com objetos).</p>
 *
 * <p>Como o conjunto é fixo, um {@code enum} dá identidade única a cada tipo
 * (comparação por {@code ==}) e suporte a {@code switch}.</p>
 */
public enum TipoPrimitivo implements Tipo {

    INT("int"),
    REAL("real"),
    STR("str"),
    BOOL("bool"),
    VOID("void"),
    NULL("null");

    private final String nome;

    TipoPrimitivo(String nome) {
        this.nome = nome;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    /**
     * Retorna o primitivo correspondente a um nome de tipo da gramática
     * ({@code "int"}, {@code "real"}, {@code "str"}, {@code "bool"}), ou
     * {@code null} se não for um primitivo.
     */
    public static TipoPrimitivo porNome(String nome) {
        for (TipoPrimitivo t : values()) {
            if (t.nome.equals(nome)) {
                return t;
            }
        }
        return null;
    }
}
