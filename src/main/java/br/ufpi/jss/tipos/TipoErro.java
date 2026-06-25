package br.ufpi.jss.tipos;

/** Tipo sentinela usado para continuar a analise apos um erro ja reportado. */
public final class TipoErro implements Tipo {

    public static final TipoErro INSTANCIA = new TipoErro();

    private TipoErro() {
    }

    @Override
    public String nome() {
        return "erro";
    }

    @Override
    public String toString() {
        return nome();
    }
}
