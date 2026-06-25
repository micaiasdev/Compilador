# -*- coding: utf-8 -*-
"""Gera o PDF didatico 'Documentacao_Classes_JSS.pdf' explicando cada classe."""
import re
import xml.sax.saxutils as su
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm
from reportlab.lib.colors import HexColor, white
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Preformatted,
                                Table, TableStyle, PageBreak, HRFlowable, KeepTogether)

AZUL = HexColor('#1F3A5F')
AZUL2 = HexColor('#2E6CA4')
CINZA = HexColor('#666666')
BOX_CONCEITO_BG = HexColor('#E8F0FE')
BOX_CONCEITO_BR = HexColor('#3F6FB0')
BOX_EXPLICA_BG = HexColor('#E9F7EF')
BOX_EXPLICA_BR = HexColor('#3C9A5F')
CODE_BG = HexColor('#F4F4F4')
CODE_BR = HexColor('#CCCCCC')

LARG = A4[0] - 4 * cm  # largura util (margens de 2cm)

ss = getSampleStyleSheet()
estilos = {
    'Titulo': ParagraphStyle('Titulo', parent=ss['Title'], fontSize=26, textColor=AZUL, leading=30),
    'Sub': ParagraphStyle('Sub', parent=ss['Normal'], fontSize=13, textColor=AZUL2, alignment=TA_CENTER, leading=18),
    'Info': ParagraphStyle('Info', parent=ss['Normal'], fontSize=10.5, textColor=CINZA, alignment=TA_CENTER, leading=15),
    'H1': ParagraphStyle('H1', parent=ss['Heading1'], fontSize=17, textColor=AZUL, spaceBefore=2, spaceAfter=2),
    'H2': ParagraphStyle('H2', parent=ss['Heading2'], fontSize=12.5, textColor=AZUL2, spaceBefore=8, spaceAfter=2),
    'Classe': ParagraphStyle('Classe', parent=ss['Heading3'], fontSize=12, textColor=AZUL, spaceBefore=8, spaceAfter=2),
    'Body': ParagraphStyle('Body', parent=ss['Normal'], fontSize=10, leading=14, alignment=TA_LEFT, spaceAfter=3),
    'Bullet': ParagraphStyle('Bullet', parent=ss['Normal'], fontSize=10, leading=13.5,
                             leftIndent=14, bulletIndent=3, spaceAfter=1),
    'Callout': ParagraphStyle('Callout', parent=ss['Normal'], fontSize=9.5, leading=13),
    'Code': ParagraphStyle('Code', parent=ss['Code'], fontSize=8.3, leading=10.5, textColor=HexColor('#202020')),
    'CellH': ParagraphStyle('CellH', parent=ss['Normal'], fontSize=9.5, leading=12, textColor=white, fontName='Helvetica-Bold'),
    'Cell': ParagraphStyle('Cell', parent=ss['Normal'], fontSize=9, leading=12),
}


def fmt(s):
    s = su.escape(s)
    s = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', s)
    s = re.sub(r'`(.+?)`', r'<font face="Courier" size="9">\1</font>', s)
    return s


def P(texto, estilo='Body'):
    return Paragraph(fmt(texto), estilos[estilo])


def bullets(itens):
    return [Paragraph(fmt(i), estilos['Bullet'], bulletText=u'•') for i in itens]


def caixa(rotulo, texto, bg, br):
    p = Paragraph('<b>' + rotulo + ':</b> ' + fmt(texto), estilos['Callout'])
    t = Table([[p]], colWidths=[LARG])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), bg),
        ('LINEBEFORE', (0, 0), (-1, -1), 3, br),
        ('LEFTPADDING', (0, 0), (-1, -1), 8), ('RIGHTPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 5), ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    return t


def codigo(texto):
    t = Table([[Preformatted(su.escape(texto), estilos['Code'])]], colWidths=[LARG])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), CODE_BG),
        ('BOX', (0, 0), (-1, -1), 0.6, CODE_BR),
        ('LEFTPADDING', (0, 0), (-1, -1), 7), ('RIGHTPADDING', (0, 0), (-1, -1), 7),
        ('TOPPADDING', (0, 0), (-1, -1), 5), ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    return t


def h1(story, texto):
    story.append(PageBreak())
    story.append(Paragraph(fmt(texto), estilos['H1']))
    story.append(HRFlowable(width='100%', thickness=1.2, color=AZUL2, spaceAfter=8, spaceBefore=2))


def classe(story, nome, pacote, resp, pontos, conceito, explicar):
    bloco = [Paragraph(fmt(nome) + '  <font size="8.5" color="#777777">(' + fmt(pacote) + ')</font>', estilos['Classe']),
             P('<b>Responsabilidade.</b> ' + resp)]
    bloco += bullets(pontos)
    story.append(KeepTogether(bloco))
    story.append(Spacer(1, 2))
    story.append(caixa('Conceito de Compiladores', conceito, BOX_CONCEITO_BG, BOX_CONCEITO_BR))
    story.append(Spacer(1, 2))
    story.append(caixa('Como explicar', explicar, BOX_EXPLICA_BG, BOX_EXPLICA_BR))
    story.append(Spacer(1, 12))


def rodape(canvas, doc):
    canvas.saveState()
    canvas.setFont('Helvetica', 8)
    canvas.setFillColor(CINZA)
    canvas.drawCentredString(A4[0] / 2.0, 1.1 * cm, 'Compilador JSS - Front-end  |  pagina %d' % doc.page)
    canvas.restoreState()


story = []

# ------------------------------------------------------------------ CAPA
story.append(Spacer(1, 3.5 * cm))
story.append(Paragraph('Compilador JSS', estilos['Titulo']))
story.append(Spacer(1, 0.2 * cm))
story.append(Paragraph('Documentacao do Front-end - Explicacao das Classes', estilos['Sub']))
story.append(Spacer(1, 1.2 * cm))
story.append(HRFlowable(width='60%', thickness=1, color=AZUL2))
story.append(Spacer(1, 0.8 * cm))
story.append(Paragraph(
    'Analise lexica, sintatica e semantica da linguagem Java Script Simplificado (JSS), '
    'implementada em Java com ANTLR 4. Este documento explica, de forma didatica, cada '
    'classe do codigo e o conceito de Compiladores que ela representa.', estilos['Info']))
story.append(Spacer(1, 2.0 * cm))
story.append(Paragraph('Disciplina de Compiladores - UFPI - 2026.1', estilos['Info']))

# ------------------------------------------------------------------ SUMARIO
story.append(PageBreak())
story.append(Paragraph('Sumario', estilos['H1']))
story.append(HRFlowable(width='100%', thickness=1.2, color=AZUL2, spaceAfter=8, spaceBefore=2))
for item in [
    '1. Visao geral e arquitetura',
    '2. A gramatica JSS.g4 (analise lexica e sintatica)',
    '3. Classes geradas pelo ANTLR',
    '4. Pacote erro (tratamento de erros)',
    '5. Pacote tipos (sistema de tipos)',
    '6. Pacote simbolos (tabela de simbolos e escopo)',
    '7. Pacote semantico (analise semantica)',
    '8. Orquestracao (Main e Compilador)',
    '9. Pacote codegen (gancho do back-end Jasmin)',
    '10. Mapa: classe -> fase do compilador',
    '11. Roteiro de apresentacao',
]:
    story.append(P(item))

# ------------------------------------------------------------------ 1. VISAO GERAL
h1(story, '1. Visao geral e arquitetura')
story.append(P(
    'O compilador le o codigo-fonte JSS pela **entrada padrao** e executa as tres fases '
    'classicas do **front-end** de um compilador, parando no **primeiro erro** encontrado. '
    'A saida e uma unica mensagem: `Linguagem compilada com sucesso` ou '
    '`Erro na linha X: descricao do erro`.'))
story.append(Spacer(1, 4))
story.append(codigo(
    "  Entrada padrao (programa.jss, em UTF-8)\n"
    "        |\n"
    "        v\n"
    "  [1] Analise lexica     -> JSSLexer            : texto  -> tokens\n"
    "        |\n"
    "        v\n"
    "  [2] Analise sintatica  -> JSSParser           : tokens -> arvore sintatica\n"
    "        |\n"
    "        v\n"
    "  [3] Analise semantica  -> AnalisadorSemantico : valida tipos, escopos e regras\n"
    "        |\n"
    "        v\n"
    '  Saida: "Linguagem compilada com sucesso"  ou  "Erro na linha X: ..."'))
story.append(Spacer(1, 6))
story.append(P(
    'Duas ideias centrais sustentam o projeto:'))
story.extend(bullets([
    '**ANTLR 4 + gramatica.** A partir de um unico arquivo de gramatica (`JSS.g4`), o ANTLR '
    '**gera** automaticamente o analisador lexico e o sintatico. Usamos o algoritmo **ALL(*)**, '
    'que resolve conflitos pela **ordem** das alternativas (nao exige uma gramatica provadamente '
    'nao-ambigua).',
    '**Padrao Visitor.** A analise semantica e um *visitor* que percorre a arvore sintatica '
    'gerada, calculando o tipo de cada expressao e validando as regras da linguagem.',
]))
story.append(Spacer(1, 4))
story.append(P('**Organizacao do codigo** (uma classe por arquivo, pacotes por responsabilidade):'))
story.extend(bullets([
    '`erro/` - representacao e reporte de erros;',
    '`tipos/` - o sistema de tipos;',
    '`simbolos/` - a tabela de simbolos e os escopos;',
    '`semantico/` - a analise semantica (o nucleo da validacao);',
    '`codegen/` - o gancho para o back-end (gerador Jasmin), ainda nao implementado;',
    '`Main` e `Compilador` - a entrada e a orquestracao das fases.',
]))

# ------------------------------------------------------------------ 2. GRAMATICA
h1(story, '2. A gramatica JSS.g4 (analise lexica e sintatica)')
classe(story, 'JSS.g4', 'src/main/antlr4',
       'A especificacao formal da linguagem JSS numa gramatica combinada (lexico + sintaxe), '
       'da qual o ANTLR gera o lexer e o parser. Sao 158 linhas escritas a mao que substituem '
       'milhares de linhas de codigo gerado.',
       ['**Lexico:** define os tokens - 21 palavras reservadas, `IDENTIFIER`, literais '
        '(`int`, `real` com expoente, `str` com escapes, `bool`), operadores, e o comentario '
        'de linha `//` (descartado);',
        '**Sintaxe:** regras para programa, declaracoes (`let`/`const`), funcoes, classes, '
        'comandos (`if`/`while`/`for`) e expressoes;',
        '**Precedencia:** fica toda em **uma** regra `expr` recursiva a esquerda, com as '
        'alternativas ordenadas da maior para a menor precedencia, e `<assoc=right>` em `**` '
        'e nas atribuicoes - exatamente a Tabela 1 da especificacao.'],
       'Gramatica livre de contexto + definicoes lexicas: a base formal das analises lexica e sintatica.',
       'E a "regra do jogo" da linguagem; eu escrevo as regras e o ANTLR constroi o lexer e o '
       'parser inteiros a partir delas.')
story.append(P('Trecho da regra de expressoes (a ordem das alternativas define a precedencia):'))
story.append(codigo(
    "expr\n"
    "  : expr '[' expr ']'                       # indexExpr   // mais alta\n"
    "  | expr '.' IDENTIFIER ('(' argList? ')')? # memberExpr  // attr/metodo; console.log\n"
    "  | op=('!'|'+'|'-'|'++'|'--') expr         # unaryExpr\n"
    "  | <assoc=right> expr '**' expr            # powExpr\n"
    "  | expr op=('*'|'/'|'%') expr              # mulExpr\n"
    "  | expr op=('+'|'-') expr                  # addExpr\n"
    "  | expr op=('<'|'>'|'<='|'>=') expr        # relExpr\n"
    "  | expr op=('=='|'!=') expr                # eqExpr\n"
    "  | expr '&&' expr                          # andExpr\n"
    "  | expr '||' expr                          # orExpr\n"
    "  | <assoc=right> expr op=('='|'+=' ...) expr  # assignExpr // mais baixa\n"
    "  | primary                                 # primaryExpr\n"
    "  ;"))

# ------------------------------------------------------------------ 3. GERADAS
h1(story, '3. Classes geradas pelo ANTLR')
story.append(P(
    'Estas quatro classes **nao sao escritas a mao**: o ANTLR as gera (cerca de 4500 linhas) '
    'a partir da `JSS.g4`, em `target/generated-sources`, e elas sao recriadas a cada build. '
    'Por isso nao ficam no controle de versao.'))
classe(story, 'JSSLexer', 'gerada pelo ANTLR',
       'O analisador lexico. Transforma o texto-fonte numa sequencia de **tokens** '
       '(palavras-chave, identificadores, numeros, strings, operadores).', [],
       'Analise lexica (scanning): de caracteres para tokens.',
       'E o "leitor de palavras": quebra o texto nas pecas minimas que o parser entende.')
classe(story, 'JSSParser', 'gerada pelo ANTLR',
       'O analisador sintatico. Usando o algoritmo **ALL(*)**, monta a **arvore sintatica** '
       '(parse tree) a partir dos tokens, conforme as regras da gramatica.', [],
       'Analise sintatica (parsing): de tokens para arvore sintatica.',
       'E o "montador da estrutura": confere se a sequencia de tokens forma um programa valido e cria a arvore.')
classe(story, 'JSSBaseVisitor / JSSVisitor', 'gerada pelo ANTLR',
       'A infraestrutura do padrao **Visitor**. `JSSVisitor` e a interface (um metodo por regra); '
       '`JSSBaseVisitor` e a implementacao base que percorre a arvore. Nosso `AnalisadorSemantico` '
       'herda dela e sobrescreve apenas os metodos que importam.', [],
       'Padrao de projeto Visitor aplicado a percorrer a arvore sintatica.',
       'E o "esqueleto de passeio" pela arvore; eu so preencho as visitas que preciso validar.')

# ------------------------------------------------------------------ 4. ERRO
h1(story, '4. Pacote erro (tratamento de erros)')
classe(story, 'ErroCompilacao', 'erro',
       'Excecao que representa qualquer erro de compilacao (lexico, sintatico ou semantico) '
       'e guarda a **linha** onde ele ocorreu.',
       ['Estende `RuntimeException` (nao checada) para poder ser lancada de dentro do ANTLR e do visitor;',
        '`mensagemFormatada()` devolve exatamente `Erro na linha X: descricao` - o formato exigido pela especificacao.'],
       'Relato de erros: o compilador precisa informar o usuario com mensagem e localizacao precisas.',
       'E a "mensagem de erro" do compilador: carrega a linha e o texto, e sabe se formatar no padrao pedido.')
classe(story, 'OuvinteErroSintatico', 'erro',
       'Ouvinte de erros do ANTLR que **interrompe a compilacao no primeiro erro** lexico ou sintatico.',
       ['Estende `BaseErrorListener`; o metodo `syntaxError` e chamado pelo lexer e pelo parser quando algo nao casa;',
        'Lanca `ErroCompilacao` na 1a ocorrencia (regra "parar no primeiro erro") e traduz a mensagem para portugues;',
        'E registrado no lexer e no parser, substituindo o ouvinte padrao do ANTLR.'],
       'Tratamento de erros lexicos e sintaticos durante a tokenizacao e o parsing.',
       'Sempre que o lexer ou o parser acha algo invalido, ele me avisa e eu paro tudo na hora, dizendo a linha.')

# ------------------------------------------------------------------ 5. TIPOS
h1(story, '5. Pacote tipos (sistema de tipos)')
classe(story, 'Tipo', 'tipos',
       'A interface base de todo o sistema de tipos do JSS.',
       ['Implementada por `TipoPrimitivo`, `TipoVetor` e `TipoClasse`;',
        'Define `nome()` - a representacao textual usada nas mensagens (ex.: `int[][]`).'],
       'Sistema de tipos: a abstracao comum que permite tratar todos os tipos de forma uniforme.',
       'E o "contrato" que todo tipo cumpre; o resto do compilador fala com tipos por esta interface.')
classe(story, 'TipoPrimitivo', 'tipos',
       'Os tipos basicos da linguagem, como um `enum`: `int`, `real`, `str`, `bool`, alem de `void` e `null`.',
       ['Sendo `enum`, cada tipo e unico (comparavel por `==`) e suporta `switch`;',
        '`porNome("int")` converte o texto da gramatica no tipo correspondente.'],
       'Tipos primitivos do sistema de tipos.',
       'Sao os tipos "de fabrica" da linguagem; uso um enum porque o conjunto e fixo e fechado.')
classe(story, 'TipoVetor', 'tipos',
       'O tipo dos vetores. Vetores **multidimensionais** sao representados por **aninhamento**.',
       ['`int[3][4]` e `TipoVetor(TipoVetor(int,4),3)`; `numDimensoes()` conta os niveis e `tipoBase()` chega ao escalar;',
        '`getElemento()` devolve o tipo apos indexar uma vez (`m[i]`);',
        'A igualdade considera tipo-base + numero de dimensoes (ignora o tamanho de cada dimensao).'],
       'Tipos compostos/derivados e como representa-los recursivamente.',
       'Um vetor 2D e "um vetor de vetores"; por isso a classe se aninha, e cada [ ] remove uma camada.')
classe(story, 'TipoClasse', 'tipos',
       'O tipo de um objeto, identificado pelo **nome** da classe.',
       ['Guarda so a identidade (nome); a estrutura detalhada (atributos/metodos) fica em `SimboloClasse`;',
        'Mantem a camada `tipos` sem depender de `simbolos` (evita dependencia circular).'],
       'Tipos definidos pelo usuario (classes).',
       'Quando declaro `let Ponto p`, o tipo de p e este TipoClasse("Ponto").')
classe(story, 'Tipos', 'tipos',
       'Utilitarios com as **regras de compatibilidade** entre tipos.',
       ['`ehNumerico`, `ehPrimitivo`; `promoverNumerico(a,b)` (real se algum for real);',
        '`compativelAtribuicao`: permite `int`->`real`, rejeita `real`->`int`, aceita `null`->objeto;',
        '`comparavelIgualdade` para `==` e `!=`.'],
       'Regras de compatibilidade e coercao (promocao) do sistema de tipos.',
       'E onde moram as regras de "combinar tipos": posso por um int onde se espera real, mas nao o contrario.')

# ------------------------------------------------------------------ 6. SIMBOLOS
h1(story, '6. Pacote simbolos (tabela de simbolos e escopo)')
classe(story, 'Simbolo', 'simbolos',
       'Uma entrada da tabela de simbolos: um identificador declarado, com seu tipo e metadados.',
       ['Guarda nome, tipo, se e constante (`const`) ou mutavel, se foi inicializado e a linha;',
        'Funcoes e classes tambem sao `Simbolo` (subclasses), para compartilharem o mesmo espaco de nomes.'],
       'Tabela de simbolos: o registro de cada identificador declarado.',
       'Cada variavel/constante/funcao/classe vira um "cadastro" destes, com tipo e se pode ser alterada.')
classe(story, 'SimboloFuncao', 'simbolos',
       'Simbolo de uma funcao (ou metodo): o tipo de **retorno** e os tipos dos **parametros**.',
       ['Usado para validar chamadas: confere a quantidade (aridade) e os tipos dos argumentos;',
        'E subclasse de `Simbolo`, entao o nome da funcao nao pode colidir com variaveis/constantes.'],
       'Assinaturas de funcoes na tabela de simbolos.',
       'Guarda a "assinatura" da funcao para eu checar se a chamada passou os argumentos certos.')
classe(story, 'SimboloClasse', 'simbolos',
       'Simbolo de uma classe: guarda o **layout** - atributos, metodos e parametros do construtor.',
       ['Atributos e metodos em `LinkedHashMap` (preserva a ordem de declaracao, util ao back-end);',
        'Usado para validar `new`, acesso a atributos (`obj.x`) e chamadas de metodo (`obj.m()`).'],
       'Tipos estruturados definidos pelo usuario, na tabela de simbolos.',
       'E a "planta" da classe: quais atributos e metodos ela tem, para validar o uso de objetos.')
classe(story, 'Escopo', 'simbolos',
       'Um nivel de escopo: um mapa nome-para-simbolo com referencia ao escopo **pai**.',
       ['O escopo global tem pai nulo; blocos (funcao, `if`, `while`, `for`) encadeiam pelo pai;',
        'Permite buscar do mais interno ao mais externo e o **shadowing**.'],
       'Escopo lexico: a visibilidade dos identificadores.',
       'Cada par de chaves { } cria um destes; ele sabe quem e o escopo "de fora" dele.')
classe(story, 'TabelaDeSimbolos', 'simbolos',
       'A **pilha de escopos** usada durante a analise semantica.',
       ['`declarar()` verifica **so o escopo atual** - redeclarar no mesmo escopo e erro;',
        '`resolver()` busca do interno ao externo - por isso o shadowing e permitido;',
        '`entrarEscopo()` / `sairEscopo()` ao abrir e fechar blocos.'],
       'Tabela de simbolos com regras de escopo e shadowing.',
       'E a memoria do analisador: ao entrar num bloco eu empilho um escopo, ao sair eu desempilho; '
       'assim sei o que esta visivel.')

# ------------------------------------------------------------------ 7. SEMANTICO
h1(story, '7. Pacote semantico (analise semantica)')
classe(story, 'AnalisadorSemantico', 'semantico',
       'O **nucleo** da analise semantica: percorre a arvore sintatica (padrao **Visitor**) '
       'validando todas as regras da linguagem.',
       ['Estende `JSSBaseVisitor<Tipo>`: cada metodo `visitX` devolve o **tipo** da expressao; comandos retornam `null`;',
        'Valida escopos, tipos de operadores, atribuicoes, `const`, `++`/`--`, funcoes '
        '(so chamaveis **depois** de declaradas) e recursao, nativas, vetores/multidim e classes '
        '(`this`, `new`, atributos, metodos);',
        'Para no **primeiro erro**, lancando `ErroCompilacao` com a linha (`ctx.getStart().getLine()`);',
        'Sobrescreve `visit()` num unico ponto para **anotar** cada expressao com seu tipo (reuso pelo back-end).'],
       'Analise semantica: verificacao de tipos e das regras de contexto que a gramatica nao captura.',
       'E o "professor rigoroso" do compilador: a sintaxe pode estar certa, mas ele confere se faz '
       'sentido (os tipos batem? a variavel existe? a funcao foi declarada?).')
classe(story, 'RegrasOperadores', 'semantico',
       'Concentra as **regras de tipo** de cada operador aritmetico, relacional e logico.',
       ['`binario(op,a,b)` e `unario(op,t)` devolvem o tipo do resultado ou lancam erro;',
        'Ex.: `+` entre int e real -> real; `%` e `**` so entre int; relacionais so entre numericos; `&&`/`||` so entre bool.'],
       'Regras de tipagem de expressoes (type checking).',
       'E a tabela de "o que cada operador aceita e o que devolve"; separei numa classe so para ficar legivel.')
classe(story, 'ValidadorNativas', 'semantico',
       'As **regras de tipo das funcoes nativas**: `input`, `console.log` e os casts.',
       ['`console.log` aceita int/real/str/bool; `input` aceita so int/real/str (bool nao);',
        '`validarCast`: qualquer primitivo vira `str`; int/real/bool sao intercambiaveis; `str` nao converte para numero.'],
       'Tratamento de funcoes embutidas (built-ins) na analise semantica.',
       'As funcoes "de fabrica" (entrada/saida e conversoes) tem regras proprias, que ficam aqui.')
classe(story, 'InfoSemantica', 'semantico',
       'Empacota o **resultado** da analise para o back-end: a arvore **anotada** com tipos, '
       'mais as tabelas de funcoes e classes.',
       ['E o "gancho" entre front-end e back-end (gerador Jasmin), evitando refazer a analise;',
        '`tipoDe(no)` devolve o tipo de qualquer expressao; tambem expoe as funcoes e classes declaradas.'],
       'Representacao intermediaria anotada: a ponte entre analise e geracao de codigo.',
       'Depois de validar, eu entrego ao back-end um "mapa" com o tipo de cada expressao e a lista de funcoes/classes.')

# ------------------------------------------------------------------ 8. ORQUESTRACAO
h1(story, '8. Orquestracao (Main e Compilador)')
classe(story, 'Compilador', 'br.ufpi.jss',
       'A **fachada** do front-end: executa o pipeline completo (lexico -> sintaxe -> semantica) '
       'e devolve a mensagem de resultado.',
       ['Liga `JSSLexer` + `JSSParser` (com o `OuvinteErroSintatico`) ao `AnalisadorSemantico`;',
        'Retorna `Linguagem compilada com sucesso` ou `Erro na linha X: ...`; e independente de '
        'stdin/stdout para poder ser testado.'],
       'Orquestracao das fases do compilador (o "driver").',
       'E o maestro que chama as tres fases na ordem certa e devolve o veredito.')
classe(story, 'Main', 'br.ufpi.jss',
       'O **ponto de entrada** do programa: le o codigo da entrada padrao e imprime o resultado.',
       ['Le `stdin` como UTF-8 e **descarta um eventual BOM** inicial (arquivos salvos com BOM, ou o pipe do PowerShell);',
        'Imprime em UTF-8 (acentos corretos) e encerra com codigo 0 (sucesso) ou 1 (erro).'],
       'Interface de linha de comando do compilador (item c da avaliacao).',
       'E o "main" de verdade: pega o que vem pela entrada padrao e mostra a resposta.')

# ------------------------------------------------------------------ 9. CODEGEN
h1(story, '9. Pacote codegen (gancho do back-end Jasmin)')
classe(story, 'GeradorJasmin', 'codegen',
       'O **esqueleto** do gerador de codigo intermediario **Jasmin** (back-end). Ainda **nao implementado**.',
       ['Estende `JSSBaseVisitor<Void>` e recebe a `InfoSemantica` produzida pelo front-end;',
        'Documenta o plano: emitir um arquivo `.j`, montado com `jasmin.jar` para gerar um `.class` '
        'executavel na JVM.'],
       'Geracao de codigo intermediario (sintese / back-end) - a proxima etapa do projeto.',
       'E o lugar reservado para transformar o programa ja validado em bytecode da JVM, via Jasmin.')

# ------------------------------------------------------------------ 10. MAPA
h1(story, '10. Mapa: classe -> fase do compilador')
linhas = [
    ['Fase / conceito', 'Onde esta no codigo'],
    ['Analise lexica (texto -> tokens)', 'JSS.g4 (regras lexicas) -> JSSLexer (gerado)'],
    ['Analise sintatica (tokens -> arvore)', 'JSS.g4 (regras de parser) -> JSSParser (gerado, ALL(*))'],
    ['Tratamento de erros (parar no 1o)', 'erro/ErroCompilacao, erro/OuvinteErroSintatico'],
    ['Sistema de tipos', 'tipos/ (Tipo, TipoPrimitivo, TipoVetor, TipoClasse, Tipos)'],
    ['Tabela de simbolos e escopo', 'simbolos/ (Simbolo, SimboloFuncao, SimboloClasse, Escopo, TabelaDeSimbolos)'],
    ['Analise semantica (type checking)', 'semantico/ (AnalisadorSemantico, RegrasOperadores, ValidadorNativas)'],
    ['Representacao intermediaria anotada', 'semantico/InfoSemantica'],
    ['Orquestracao / linha de comando', 'Compilador, Main'],
    ['Geracao de codigo (back-end, futuro)', 'codegen/GeradorJasmin'],
]
dados = [[Paragraph(c, estilos['CellH']) for c in linhas[0]]]
for row in linhas[1:]:
    dados.append([Paragraph(fmt(row[0]), estilos['Cell']), Paragraph(fmt(row[1]), estilos['Cell'])])
tab = Table(dados, colWidths=[0.42 * LARG, 0.58 * LARG])
tab.setStyle(TableStyle([
    ('BACKGROUND', (0, 0), (-1, 0), AZUL),
    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [white, HexColor('#F2F6FB')]),
    ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#BBBBBB')),
    ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ('LEFTPADDING', (0, 0), (-1, -1), 6), ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ('TOPPADDING', (0, 0), (-1, -1), 5), ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
]))
story.append(tab)

# ------------------------------------------------------------------ 11. ROTEIRO
h1(story, '11. Roteiro de apresentacao')
story.append(P('Uma ordem sugerida para apresentar ao professor:'))
story.extend(bullets([
    '**Visao geral.** Mostre o pipeline das tres fases e que o ANTLR gera o lexer e o parser a partir da gramatica.',
    '**Gramatica.** Abra a `JSS.g4`: mostre alguns tokens e a regra `expr`, explicando que a **ordem** das alternativas define a precedencia.',
    '**Demonstracao.** Rode um exemplo valido e um com erro (use `exemplos/ok` e `exemplos/erros`), mostrando a linha exata do erro.',
    '**Erros.** Explique o `OuvinteErroSintatico` e a regra de **parar no primeiro erro**.',
    '**Simbolos e tipos.** Mostre a `TabelaDeSimbolos` (escopo e shadowing) e o pacote `tipos` (ex.: `int`->`real` permitido, `real`->`int` nao).',
    '**Semantica.** Apresente o `AnalisadorSemantico` como o Visitor que une tudo; use 1-2 regras (ex.: o `+` e a atribuicao com promocao numerica).',
    '**Back-end.** Feche mostrando os ganchos: `InfoSemantica` (arvore anotada) e o esqueleto `GeradorJasmin`.',
]))
story.append(Spacer(1, 6))
story.append(caixa('Decisoes para destacar',
                   'parar no **primeiro erro** com a linha; suporte a **vetores multidimensionais** '
                   '(`int[2][3]`); `console.log` reconhecido **sem** tornar `console`/`log` palavras '
                   'reservadas; `main` **opcional** e sem parametros; `float` nao existe (o correto e `real`).',
                   BOX_CONCEITO_BG, BOX_CONCEITO_BR))

# ------------------------------------------------------------------ BUILD
doc = SimpleDocTemplate('Documentacao_Classes_JSS.pdf', pagesize=A4,
                        leftMargin=2 * cm, rightMargin=2 * cm, topMargin=2 * cm, bottomMargin=1.8 * cm,
                        title='Compilador JSS - Documentacao das Classes', author='Front-end JSS')
doc.build(story, onLaterPages=rodape, onFirstPage=rodape)
print('PDF gerado: Documentacao_Classes_JSS.pdf')
