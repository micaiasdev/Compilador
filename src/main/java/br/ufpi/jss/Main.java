package br.ufpi.jss;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Ponto de entrada do compilador da linguagem Java Script Simplificado (JSS).
 *
 * <p>Lê o código-fonte da <b>entrada padrão</b> (UTF-8), executa o front-end via
 * {@link Compilador} e imprime na saída padrão (UTF-8):</p>
 * <ul>
 *   <li>{@code Linguagem compilada com sucesso}; ou</li>
 *   <li>{@code Erro na linha X: descrição do erro} (parando no primeiro erro).</li>
 * </ul>
 *
 * <p>Uso: {@code java -jar jss-compiler.jar < programa.jss}</p>
 */
public final class Main {

    /** Marca de ordem de byte (BOM) do UTF-8, em forma de caractere (U+FEFF). */
    private static final char BOM = '﻿';

    private Main() {
        // Classe utilitária: não deve ser instanciada.
    }

    public static void main(String[] args) throws IOException {
        // Força UTF-8 na saída para que os acentos das mensagens saiam corretos
        // independentemente da codificação padrão do console.
        PrintStream saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        // Lê todo o código-fonte da entrada padrão como UTF-8 e remove um eventual
        // BOM inicial (arquivos salvos com BOM, ou o pipe do PowerShell, o inserem).
        String fonte = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        if (!fonte.isEmpty() && fonte.charAt(0) == BOM) {
            fonte = fonte.substring(1);
        }

        String resultado = new Compilador().compilar(fonte);
        saida.println(resultado);
        if (!Compilador.SUCESSO.equals(resultado)) {
            System.exit(1); // facilita scripts de teste; a mensagem já foi impressa
        }
    }
}
