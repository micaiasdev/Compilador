package br.ufpi.jss.semantico;

import br.ufpi.jss.JSSBaseVisitor;
import br.ufpi.jss.JSSParser.*;
import br.ufpi.jss.erro.ErroCompilacao;
import br.ufpi.jss.simbolos.Simbolo;
import br.ufpi.jss.simbolos.SimboloClasse;
import br.ufpi.jss.simbolos.SimboloFuncao;
import br.ufpi.jss.simbolos.TabelaDeSimbolos;
import br.ufpi.jss.tipos.Tipo;
import br.ufpi.jss.tipos.TipoClasse;
import br.ufpi.jss.tipos.TipoPrimitivo;
import br.ufpi.jss.tipos.TipoVetor;
import br.ufpi.jss.tipos.Tipos;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static br.ufpi.jss.tipos.TipoPrimitivo.BOOL;
import static br.ufpi.jss.tipos.TipoPrimitivo.INT;
import static br.ufpi.jss.tipos.TipoPrimitivo.VOID;

/**
 * Análise semântica do JSS: percorre a árvore sintática validando escopos,
 * declarações, tipos, operadores, funções, funções nativas, vetores
 * (inclusive multidimensionais) e classes.
 *
 * <p>Retorna o {@link Tipo} de cada expressão; comandos e declarações retornam
 * {@code null}. No primeiro problema, lança {@link ErroCompilacao} (com a linha),
 * encerrando a análise — conforme a regra de "parar no primeiro erro".</p>
 */
public class AnalisadorSemantico extends JSSBaseVisitor<Tipo> {

    private final TabelaDeSimbolos tabela = new TabelaDeSimbolos();

    /** Função/método em análise (para validar {@code return}); {@code null} no topo. */
    private SimboloFuncao funcaoAtual;

    /** Classe em análise (para validar {@code this}); {@code null} fora de classes. */
    private SimboloClasse classeAtual;

    /** Profundidade de laços (para validar {@code break}). */
    private int profundidadeLaco;

    // --- Anotações reaproveitadas pelo back-end (gerador Jasmin) ---

    /** Tipo resolvido de cada expressão (árvore anotada). */
    private final ParseTreeProperty<Tipo> tipos = new ParseTreeProperty<>();

    /** Funções globais declaradas, na ordem de declaração. */
    private final Map<String, SimboloFuncao> funcoes = new LinkedHashMap<>();

    /** Classes declaradas (layout de atributos, métodos e construtor). */
    private final Map<String, SimboloClasse> classes = new LinkedHashMap<>();

    /**
     * Anota cada expressão com o seu tipo enquanto a árvore é percorrida. Como
     * toda subexpressão é avaliada através de {@link #visit(ParseTree)}, este
     * único ponto basta para construir a "árvore anotada" usada pelo back-end.
     */
    @Override
    public Tipo visit(ParseTree arvore) {
        Tipo t = super.visit(arvore);
        if (t != null && (arvore instanceof ExprContext || arvore instanceof PrimaryContext)) {
            tipos.put(arvore, t);
        }
        return t;
    }

    /** Resultado da análise para o back-end (tipos anotados + funções + classes). */
    public InfoSemantica getInfo() {
        return new InfoSemantica(tipos, funcoes, classes);
    }

    // =====================================================================
    // Declarações de topo
    // =====================================================================

    @Override
    public Tipo visitVarDecl(VarDeclContext ctx) {
        declararVariaveis(ctx.type(), ctx.arrayDim(), ctx.varInit(), false);
        return null;
    }

    @Override
    public Tipo visitConstDecl(ConstDeclContext ctx) {
        int linha = ctx.getStart().getLine();
        Tipo tipo = resolverTipo(ctx.type(), ctx.arrayDim());
        validarInicializador(tipo, ctx.initializer(), linha);
        tabela.declarar(new Simbolo(ctx.IDENTIFIER().getText(), tipo, true, true, linha));
        return null;
    }

    @Override
    public Tipo visitFuncDecl(FuncDeclContext ctx) {
        int linha = ctx.getStart().getLine();
        String nome = ctx.IDENTIFIER().getText();
        Tipo retorno = resolverReturnType(ctx.returnType());
        SimboloFuncao funcao = new SimboloFuncao(nome, retorno, tiposDeParametros(ctx.paramList()), linha);

        // Registra ANTES do corpo: habilita recursão e impede colisão de nomes.
        tabela.declarar(funcao);
        funcoes.put(nome, funcao);

        if (nome.equals("main") && funcao.aridade() != 0) {
            throw erro(linha, "a função 'main' não pode ter parâmetros");
        }

        SimboloFuncao anterior = funcaoAtual;
        funcaoAtual = funcao;
        tabela.entrarEscopo();
        declararParametros(ctx.paramList());
        for (StatementContext s : ctx.block().statement()) {
            visit(s);
        }
        exigirRetornoObrigatorio("funcao", nome, retorno, ctx.block(), linha);
        tabela.sairEscopo();
        funcaoAtual = anterior;
        return null;
    }

    @Override
    public Tipo visitClassDecl(ClassDeclContext ctx) {
        int linha = ctx.getStart().getLine();
        String nome = ctx.IDENTIFIER().getText();
        SimboloClasse classe = new SimboloClasse(nome, linha);
        tabela.declarar(classe); // registra o nome antes dos membros (permite auto-referência)
        classes.put(nome, classe);

        // 1ª passada: assinaturas (atributos, construtor, métodos)
        for (AttrDeclContext a : ctx.attrDecl()) {
            String an = a.IDENTIFIER().getText();
            if (classe.temAtributo(an)) {
                throw erro(a.getStart().getLine(), "atributo '" + an + "' já declarado na classe '" + nome + "'");
            }
            classe.adicionarAtributo(an, resolverTipo(a.type(), a.arrayDim()));
        }
        ConstructorDeclContext ctor = ctx.constructorDecl();
        if (!ctor.IDENTIFIER().getText().equals(nome)) {
            throw erro(ctor.getStart().getLine(), "o construtor deve ter o nome da classe '" + nome + "'");
        }
        classe.setParametrosConstrutor(tiposDeParametros(ctor.paramList()));
        for (MethodDeclContext m : ctx.methodDecl()) {
            String mn = m.IDENTIFIER().getText();
            if (classe.temMetodo(mn)) {
                throw erro(m.getStart().getLine(), "método '" + mn + "' já declarado na classe '" + nome + "'");
            }
            Tipo ret = resolverReturnType(m.returnType());
            classe.adicionarMetodo(new SimboloFuncao(mn, ret, tiposDeParametros(m.paramList()), m.getStart().getLine()));
        }

        // 2ª passada: corpos (com a estrutura da classe já conhecida)
        classeAtual = classe;
        analisarCorpoConstrutor(ctor, classe);
        for (MethodDeclContext m : ctx.methodDecl()) {
            analisarCorpoMetodo(m, classe);
        }
        classeAtual = null;
        return null;
    }

    // =====================================================================
    // Comandos
    // =====================================================================

    @Override
    public Tipo visitBlock(BlockContext ctx) {
        tabela.entrarEscopo();
        for (StatementContext s : ctx.statement()) {
            visit(s);
        }
        tabela.sairEscopo();
        return null;
    }

    @Override
    public Tipo visitIfStmt(IfStmtContext ctx) {
        exigirCondicaoBool(ctx.expr(), "if");
        visit(ctx.block());
        for (ElseIfContext ei : ctx.elseIf()) {
            exigirCondicaoBool(ei.expr(), "else if");
            visit(ei.block());
        }
        if (ctx.elseBlock() != null) {
            visit(ctx.elseBlock().block());
        }
        return null;
    }

    @Override
    public Tipo visitWhileStmt(WhileStmtContext ctx) {
        exigirCondicaoBool(ctx.expr(), "while");
        profundidadeLaco++;
        visit(ctx.block());
        profundidadeLaco--;
        return null;
    }

    @Override
    public Tipo visitForStmt(ForStmtContext ctx) {
        tabela.entrarEscopo();
        ForInitContext init = ctx.forInit();
        if (init instanceof ForInitDeclContext) {
            ForInitDeclContext d = (ForInitDeclContext) init;
            declararVariaveis(d.type(), d.arrayDim(), d.varInit(), false);
        } else if (init instanceof ForInitExprContext) {
            for (ExprContext e : ((ForInitExprContext) init).exprList().expr()) {
                visit(e);
            }
        }
        if (ctx.expr() != null) {
            exigirCondicaoBool(ctx.expr(), "for");
        }
        if (ctx.forUpdate() != null) {
            for (ExprContext e : ctx.forUpdate().exprList().expr()) {
                visit(e);
            }
        }
        profundidadeLaco++;
        visit(ctx.block());
        profundidadeLaco--;
        tabela.sairEscopo();
        return null;
    }

    @Override
    public Tipo visitReturnStmt(ReturnStmtContext ctx) {
        int linha = ctx.getStart().getLine();
        if (funcaoAtual == null) {
            throw erro(linha, "'return' fora de uma função");
        }
        Tipo retorno = funcaoAtual.getRetorno();
        if (ctx.expr() != null) {
            Tipo t = visit(ctx.expr());
            if (retorno == VOID) {
                throw erro(linha, "função 'void' não pode retornar um valor");
            }
            if (!Tipos.compativelAtribuicao(retorno, t)) {
                throw erro(linha, "tipo de retorno incompatível: esperado " + retorno.nome()
                    + ", encontrado " + t.nome());
            }
        } else if (retorno != VOID) {
            throw erro(linha, "'return' sem valor em função que retorna " + retorno.nome());
        }
        return null;
    }

    @Override
    public Tipo visitBreakStmt(BreakStmtContext ctx) {
        if (profundidadeLaco == 0) {
            throw erro(ctx.getStart().getLine(), "'break' fora de um laço (while/for)");
        }
        return null;
    }

    @Override
    public Tipo visitExprStmt(ExprStmtContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // =====================================================================
    // Expressões — primários
    // =====================================================================

    @Override
    public Tipo visitPrimaryExpr(PrimaryExprContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public Tipo visitParenExpr(ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Tipo visitLiteralExpr(LiteralExprContext ctx) {
        LiteralContext l = ctx.literal();
        if (l.INT_LIT() != null) {
            return TipoPrimitivo.INT;
        }
        if (l.REAL_LIT() != null) {
            return TipoPrimitivo.REAL;
        }
        if (l.STR_LIT() != null) {
            return TipoPrimitivo.STR;
        }
        if (l.TRUE() != null || l.FALSE() != null) {
            return TipoPrimitivo.BOOL;
        }
        return TipoPrimitivo.NULL; // 'null'
    }

    @Override
    public Tipo visitIdExpr(IdExprContext ctx) {
        int linha = ctx.getStart().getLine();
        String nome = ctx.IDENTIFIER().getText();
        Simbolo s = tabela.resolver(nome);
        if (s == null) {
            throw erro(linha, "identificador '" + nome + "' não declarado");
        }
        if (s instanceof SimboloFuncao) {
            throw erro(linha, "'" + nome + "' é uma função e não pode ser usada como valor");
        }
        if (s instanceof SimboloClasse) {
            throw erro(linha, "'" + nome + "' é uma classe e não pode ser usada como valor");
        }
        return s.getTipo();
    }

    @Override
    public Tipo visitThisExpr(ThisExprContext ctx) {
        if (classeAtual == null) {
            throw erro(ctx.getStart().getLine(), "'this' só pode ser usado dentro de uma classe");
        }
        return classeAtual.getTipoClasse();
    }

    @Override
    public Tipo visitCastExpr(CastExprContext ctx) {
        Tipo arg = visit(ctx.expr());
        return ValidadorNativas.validarCast(ctx.castType().getText(), arg, ctx.getStart().getLine());
    }

    @Override
    public Tipo visitNewExpr(NewExprContext ctx) {
        int linha = ctx.getStart().getLine();
        String nome = ctx.IDENTIFIER().getText();
        Simbolo s = tabela.resolver(nome);
        if (!(s instanceof SimboloClasse)) {
            throw erro(linha, "classe '" + nome + "' não declarada");
        }
        SimboloClasse classe = (SimboloClasse) s;
        verificarArgumentos("construtor de '" + nome + "'", classe.getParametrosConstrutor(),
            argumentos(ctx.argList()), linha);
        return classe.getTipoClasse();
    }

    // =====================================================================
    // Expressões — pós-fixadas (índice, membro, chamada)
    // =====================================================================

    @Override
    public Tipo visitIndexExpr(IndexExprContext ctx) {
        Tipo arr = visit(ctx.expr(0));
        Tipo idx = visit(ctx.expr(1));
        if (idx != INT) {
            throw erro(ctx.expr(1).getStart().getLine(),
                "índice de vetor deve ser do tipo int (encontrado " + idx.nome() + ")");
        }
        if (!(arr instanceof TipoVetor)) {
            throw erro(ctx.getStart().getLine(),
                "indexação aplicada a um valor que não é vetor (tipo " + arr.nome() + ")");
        }
        return ((TipoVetor) arr).getElemento();
    }

    @Override
    public Tipo visitMemberExpr(MemberExprContext ctx) {
        int linha = ctx.getStart().getLine();
        ExprContext receptor = ctx.expr();
        String membro = ctx.IDENTIFIER().getText();
        boolean chamada = ctx.LPAREN() != null;

        // console.log(...) — nativa, desde que 'console' não seja uma variável declarada
        if (chamada && membro.equals("log") && ehIdentificador(receptor, "console")
                && tabela.resolver("console") == null) {
            for (ExprContext arg : argumentos(ctx.argList())) {
                ValidadorNativas.validarTipoConsoleLog(visit(arg), arg.getStart().getLine());
            }
            return VOID;
        }

        Tipo tipoReceptor = visit(receptor);
        if (!(tipoReceptor instanceof TipoClasse)) {
            throw erro(linha, "acesso a '." + membro + "' em um valor que não é objeto (tipo "
                + tipoReceptor.nome() + ")");
        }
        SimboloClasse classe = classeDoTipo((TipoClasse) tipoReceptor, linha);

        if (chamada) {
            SimboloFuncao metodo = classe.metodo(membro);
            if (metodo == null) {
                throw erro(linha, "método '" + membro + "' não declarado na classe '" + classe.getNome() + "'");
            }
            verificarArgumentos("método '" + membro + "'", metodo.getParametros(),
                argumentos(ctx.argList()), linha);
            return metodo.getRetorno();
        }
        if (!classe.temAtributo(membro)) {
            throw erro(linha, "atributo '" + membro + "' não declarado na classe '" + classe.getNome() + "'");
        }
        return classe.tipoAtributo(membro);
    }

    @Override
    public Tipo visitCallExpr(CallExprContext ctx) {
        int linha = ctx.getStart().getLine();
        ExprContext callee = ctx.expr();
        List<ExprContext> args = argumentos(ctx.argList());

        IdExprContext id = comoIdentificador(callee);
        if (id != null) {
            String nome = id.IDENTIFIER().getText();

            if (nome.equals("input") && tabela.resolver("input") == null) {
                validarInput(args, linha);
                return VOID;
            }

            Simbolo s = tabela.resolver(nome);
            if (s instanceof SimboloClasse) {
                throw erro(linha, "'" + nome + "' é uma classe; use 'new " + nome + "(...)' para criar um objeto");
            }
            if (!(s instanceof SimboloFuncao)) {
                throw erro(linha, "função '" + nome + "' não declarada");
            }
            SimboloFuncao f = (SimboloFuncao) s;
            verificarArgumentos("função '" + nome + "'", f.getParametros(), args, linha);
            return f.getRetorno();
        }
        throw erro(linha, "expressão não é uma função e não pode ser chamada");
    }

    // =====================================================================
    // Expressões — operadores
    // =====================================================================

    @Override
    public Tipo visitUnaryExpr(UnaryExprContext ctx) {
        int linha = ctx.getStart().getLine();
        String op = ctx.op.getText();
        if (op.equals("++") || op.equals("--")) {
            return validarIncrementoDecremento(ctx.expr(), op, linha);
        }
        return RegrasOperadores.unario(op, visit(ctx.expr()), linha);
    }

    @Override
    public Tipo visitPostfixExpr(PostfixExprContext ctx) {
        return validarIncrementoDecremento(ctx.expr(), ctx.op.getText(), ctx.getStart().getLine());
    }

    @Override
    public Tipo visitPowExpr(PowExprContext ctx) {
        return RegrasOperadores.binario("**", visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.getStart().getLine());
    }

    @Override
    public Tipo visitMulExpr(MulExprContext ctx) {
        return RegrasOperadores.binario(ctx.op.getText(), visit(ctx.expr(0)), visit(ctx.expr(1)),
            ctx.getStart().getLine());
    }

    @Override
    public Tipo visitAddExpr(AddExprContext ctx) {
        return RegrasOperadores.binario(ctx.op.getText(), visit(ctx.expr(0)), visit(ctx.expr(1)),
            ctx.getStart().getLine());
    }

    @Override
    public Tipo visitRelExpr(RelExprContext ctx) {
        return RegrasOperadores.binario(ctx.op.getText(), visit(ctx.expr(0)), visit(ctx.expr(1)),
            ctx.getStart().getLine());
    }

    @Override
    public Tipo visitEqExpr(EqExprContext ctx) {
        return RegrasOperadores.binario(ctx.op.getText(), visit(ctx.expr(0)), visit(ctx.expr(1)),
            ctx.getStart().getLine());
    }

    @Override
    public Tipo visitAndExpr(AndExprContext ctx) {
        return RegrasOperadores.binario("&&", visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.getStart().getLine());
    }

    @Override
    public Tipo visitOrExpr(OrExprContext ctx) {
        return RegrasOperadores.binario("||", visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.getStart().getLine());
    }

    @Override
    public Tipo visitAssignExpr(AssignExprContext ctx) {
        int linha = ctx.getStart().getLine();
        String op = ctx.op.getText();
        ExprContext alvoCtx = ctx.expr(0);

        Tipo tipoAlvo = analisarAlvo(alvoCtx);
        if (!alvoMutavel(alvoCtx)) {
            throw erro(linha, "não é possível atribuir a uma constante");
        }
        Tipo tipoValor = visit(ctx.expr(1));

        if (op.equals("=")) {
            if (!Tipos.compativelAtribuicao(tipoAlvo, tipoValor)) {
                throw erro(linha, "não é possível atribuir " + tipoValor.nome() + " a " + tipoAlvo.nome());
            }
        } else {
            String base = op.substring(0, op.length() - 1); // "+=" -> "+", "**=" -> "**"
            Tipo resultado = RegrasOperadores.binario(base, tipoAlvo, tipoValor, linha);
            if (!Tipos.compativelAtribuicao(tipoAlvo, resultado)) {
                throw erro(linha, "não é possível atribuir " + resultado.nome() + " a " + tipoAlvo.nome());
            }
        }
        return tipoAlvo;
    }

    // =====================================================================
    // Auxiliares — declarações e tipos
    // =====================================================================

    private void declararVariaveis(TypeContext typeCtx, ArrayDimContext dimCtx,
                                   List<VarInitContext> inits, boolean constante) {
        Tipo tipo = resolverTipo(typeCtx, dimCtx);
        for (VarInitContext vi : inits) {
            int linha = vi.getStart().getLine();
            boolean temInit = vi.initializer() != null;
            if (temInit) {
                validarInicializador(tipo, vi.initializer(), linha);
            }
            tabela.declarar(new Simbolo(vi.IDENTIFIER().getText(), tipo, constante, temInit, linha));
        }
    }

    private void validarInicializador(Tipo tipoDeclarado, InitializerContext ini, int linha) {
        if (ini.arrayLiteral() != null) {
            if (!(tipoDeclarado instanceof TipoVetor)) {
                throw erro(linha, "literal de vetor atribuído a um tipo não-vetor (" + tipoDeclarado.nome() + ")");
            }
            validarLiteralVetor(ini.arrayLiteral(), (TipoVetor) tipoDeclarado);
        } else {
            Tipo t = visit(ini.expr());
            if (!Tipos.compativelAtribuicao(tipoDeclarado, t)) {
                throw erro(linha, "não é possível atribuir " + t.nome() + " a " + tipoDeclarado.nome());
            }
        }
    }

    /** Valida um literal aninhado quanto à profundidade (dimensões) e tipos dos elementos. */
    private void validarLiteralVetor(ArrayLiteralContext lit, TipoVetor esperado) {
        Tipo elemento = esperado.getElemento();
        for (ArrayElemContext el : lit.arrayElem()) {
            int linha = el.getStart().getLine();
            if (elemento instanceof TipoVetor) {
                if (el.arrayLiteral() == null) {
                    throw erro(linha, "esperado um vetor aninhado no literal (tipo " + elemento.nome() + ")");
                }
                validarLiteralVetor(el.arrayLiteral(), (TipoVetor) elemento);
            } else {
                if (el.expr() == null) {
                    throw erro(linha, "esperado um valor " + elemento.nome() + ", encontrado vetor aninhado");
                }
                Tipo t = visit(el.expr());
                if (!Tipos.compativelAtribuicao(elemento, t)) {
                    throw erro(linha, "elemento de tipo " + t.nome() + " incompatível com " + elemento.nome());
                }
            }
        }
    }

    private Tipo resolverTipo(TypeContext typeCtx, ArrayDimContext dimCtx) {
        Tipo base = resolverTipoBase(typeCtx);
        if (dimCtx == null) {
            return base;
        }
        for (ExprContext dim : dimCtx.expr()) { // dimensões informadas devem ser int
            Tipo td = visit(dim);
            if (td != INT) {
                throw erro(dim.getStart().getLine(), "dimensão de vetor deve ser do tipo int (encontrado "
                    + td.nome() + ")");
            }
        }
        Tipo tipo = base;
        for (int i = 0; i < dimCtx.LBRACK().size(); i++) {
            tipo = new TipoVetor(tipo, TipoVetor.DIMENSAO_NAO_ESPECIFICADA);
        }
        return tipo;
    }

    private Tipo resolverTipoBase(TypeContext typeCtx) {
        if (typeCtx.IDENTIFIER() != null) {
            String nome = typeCtx.IDENTIFIER().getText();
            Simbolo s = tabela.resolver(nome);
            if (!(s instanceof SimboloClasse)) {
                throw erro(typeCtx.getStart().getLine(), "tipo '" + nome + "' não declarado");
            }
            return ((SimboloClasse) s).getTipoClasse();
        }
        return TipoPrimitivo.porNome(typeCtx.getText());
    }

    private Tipo resolverReturnType(ReturnTypeContext rt) {
        return rt.VOID() != null ? VOID : resolverTipo(rt.type(), rt.arrayDim());
    }

    // =====================================================================
    // Auxiliares — funções, classes, argumentos
    // =====================================================================

    private List<Tipo> tiposDeParametros(ParamListContext pl) {
        List<Tipo> tipos = new ArrayList<>();
        if (pl != null) {
            for (ParamContext p : pl.param()) {
                tipos.add(resolverTipo(p.type(), p.arrayDim()));
            }
        }
        return tipos;
    }

    private void declararParametros(ParamListContext pl) {
        if (pl == null) {
            return;
        }
        for (ParamContext p : pl.param()) {
            int linha = p.getStart().getLine();
            tabela.declarar(new Simbolo(p.IDENTIFIER().getText(), resolverTipo(p.type(), p.arrayDim()),
                false, true, linha));
        }
    }

    private void analisarCorpoConstrutor(ConstructorDeclContext ctor, SimboloClasse classe) {
        tabela.entrarEscopo();
        declararParametros(ctor.paramList());
        for (StatementContext s : ctor.block().statement()) {
            visit(s);
        }
        tabela.sairEscopo();
    }

    private void analisarCorpoMetodo(MethodDeclContext m, SimboloClasse classe) {
        SimboloFuncao anterior = funcaoAtual;
        funcaoAtual = classe.metodo(m.IDENTIFIER().getText());
        tabela.entrarEscopo();
        declararParametros(m.paramList());
        for (StatementContext s : m.block().statement()) {
            visit(s);
        }
        exigirRetornoObrigatorio("metodo", m.IDENTIFIER().getText(), funcaoAtual.getRetorno(),
            m.block(), m.getStart().getLine());
        tabela.sairEscopo();
        funcaoAtual = anterior;
    }

    private void verificarArgumentos(String descricao, List<Tipo> parametros, List<ExprContext> args, int linha) {
        if (args.size() != parametros.size()) {
            throw erro(linha, descricao + " espera " + parametros.size() + " argumento(s), recebeu " + args.size());
        }
        for (int i = 0; i < args.size(); i++) {
            Tipo ta = visit(args.get(i));
            if (!Tipos.compativelAtribuicao(parametros.get(i), ta)) {
                throw erro(args.get(i).getStart().getLine(), "argumento " + (i + 1) + " de " + descricao
                    + ": esperado " + parametros.get(i).nome() + ", recebido " + ta.nome());
            }
        }
    }

    private void validarInput(List<ExprContext> args, int linha) {
        for (ExprContext arg : args) {
            IdExprContext id = comoIdentificador(arg);
            if (id == null) {
                throw erro(arg.getStart().getLine(), "input requer variáveis (não literais ou expressões)");
            }
            String nome = id.IDENTIFIER().getText();
            Simbolo s = tabela.resolver(nome);
            if (s == null) {
                throw erro(arg.getStart().getLine(), "identificador '" + nome + "' não declarado");
            }
            if (s instanceof SimboloFuncao || s instanceof SimboloClasse) {
                throw erro(arg.getStart().getLine(), "input requer uma variável (recebido '" + nome + "')");
            }
            if (!s.isMutavel()) {
                throw erro(arg.getStart().getLine(), "input não pode escrever na constante '" + nome + "'");
            }
            ValidadorNativas.validarTipoInput(s.getTipo(), arg.getStart().getLine());
        }
    }

    // =====================================================================
    // Auxiliares — lvalue (atribuição, ++/--)
    // =====================================================================

    private Tipo analisarAlvo(ExprContext alvo) {
        int linha = alvo.getStart().getLine();
        IdExprContext id = comoIdentificador(alvo);
        if (id != null) {
            String nome = id.IDENTIFIER().getText();
            Simbolo s = tabela.resolver(nome);
            if (s == null) {
                throw erro(linha, "identificador '" + nome + "' não declarado");
            }
            if (s instanceof SimboloFuncao || s instanceof SimboloClasse) {
                throw erro(linha, "'" + nome + "' não é uma variável");
            }
            return s.getTipo();
        }
        if (alvo instanceof IndexExprContext) {
            return visit(alvo);
        }
        if (alvo instanceof MemberExprContext && ((MemberExprContext) alvo).LPAREN() == null) {
            return visit(alvo);
        }
        throw erro(linha, "lado esquerdo da atribuição não é atribuível");
    }

    private boolean alvoMutavel(ExprContext alvo) {
        if (ehThis(alvo)) {
            return true;
        }
        IdExprContext id = comoIdentificador(alvo);
        if (id != null) {
            Simbolo s = tabela.resolver(id.IDENTIFIER().getText());
            return s != null && s.isMutavel();
        }
        if (alvo instanceof IndexExprContext) {
            return alvoMutavel(((IndexExprContext) alvo).expr(0));
        }
        if (alvo instanceof MemberExprContext) {
            return alvoMutavel(((MemberExprContext) alvo).expr());
        }
        return false;
    }

    private Tipo validarIncrementoDecremento(ExprContext operando, String op, int linha) {
        Tipo tipo = analisarAlvo(operando);
        if (!Tipos.ehNumerico(tipo)) {
            throw erro(linha, "operador '" + op + "' requer uma variável numérica (int ou real)");
        }
        if (!alvoMutavel(operando)) {
            throw erro(linha, "não é possível aplicar '" + op + "' a uma constante");
        }
        return tipo;
    }

    // =====================================================================
    // Auxiliares — diversos
    // =====================================================================

    private void exigirRetornoObrigatorio(String categoria, String nome, Tipo retorno, BlockContext block, int linha) {
        if (retorno != VOID && !blocoGaranteRetorno(block)) {
            throw erro(linha, categoria + " '" + nome + "' deve retornar " + retorno.nome());
        }
    }

    private boolean blocoGaranteRetorno(BlockContext block) {
        for (StatementContext s : block.statement()) {
            if (statementGaranteRetorno(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean statementGaranteRetorno(StatementContext statement) {
        if (statement.returnStmt() != null) {
            return true;
        }
        if (statement.block() != null) {
            return blocoGaranteRetorno(statement.block());
        }
        if (statement.ifStmt() != null) {
            return ifGaranteRetorno(statement.ifStmt());
        }
        return false;
    }

    private boolean ifGaranteRetorno(IfStmtContext ifStmt) {
        if (ifStmt.elseBlock() == null || !blocoGaranteRetorno(ifStmt.block())) {
            return false;
        }
        for (ElseIfContext elseIf : ifStmt.elseIf()) {
            if (!blocoGaranteRetorno(elseIf.block())) {
                return false;
            }
        }
        return blocoGaranteRetorno(ifStmt.elseBlock().block());
    }

    private void exigirCondicaoBool(ExprContext cond, String construtor) {
        Tipo t = visit(cond);
        if (t != BOOL) {
            throw erro(cond.getStart().getLine(),
                "a condição de '" + construtor + "' deve ser bool (encontrado " + t.nome() + ")");
        }
    }

    private SimboloClasse classeDoTipo(TipoClasse tipo, int linha) {
        Simbolo s = tabela.resolver(tipo.nome());
        if (s instanceof SimboloClasse) {
            return (SimboloClasse) s;
        }
        throw erro(linha, "classe '" + tipo.nome() + "' não declarada");
    }

    /**
     * Desembrulha uma expressão para o seu primário, quando ela for apenas um
     * primário (ex.: um identificador). Em {@code expr}, um primário aparece
     * envolto em {@code PrimaryExprContext}, pois {@code primary} é uma regra à
     * parte (logo {@code IdExprContext}/{@code ThisExprContext} estendem
     * {@code PrimaryContext}, não {@code ExprContext}).
     */
    private static PrimaryContext primarioDe(ExprContext e) {
        return (e instanceof PrimaryExprContext) ? ((PrimaryExprContext) e).primary() : null;
    }

    /** Retorna o {@code IdExprContext} se a expressão for apenas um identificador, senão {@code null}. */
    private static IdExprContext comoIdentificador(ExprContext e) {
        PrimaryContext p = primarioDe(e);
        return (p instanceof IdExprContext) ? (IdExprContext) p : null;
    }

    /** {@code true} se a expressão for apenas {@code this}. */
    private static boolean ehThis(ExprContext e) {
        return primarioDe(e) instanceof ThisExprContext;
    }

    private static boolean ehIdentificador(ExprContext e, String nome) {
        IdExprContext id = comoIdentificador(e);
        return id != null && id.IDENTIFIER().getText().equals(nome);
    }

    private static List<ExprContext> argumentos(ArgListContext argList) {
        return argList != null ? argList.expr() : List.of();
    }

    private static ErroCompilacao erro(int linha, String descricao) {
        return new ErroCompilacao(linha, descricao);
    }
}
