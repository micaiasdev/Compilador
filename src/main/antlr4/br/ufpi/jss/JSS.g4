/*
 * Gramática da linguagem Java Script Simplificado (JSS).
 *
 * Gramática combinada (léxico + sintaxe) para o front-end do compilador.
 * O antlr4-maven-plugin deriva o pacote Java do caminho deste arquivo
 * (src/main/antlr4/br/ufpi/jss) -> pacote "br.ufpi.jss".
 *
 * Estratégia de precedência/associatividade: uma única regra `expr` recursiva
 * à esquerda, com a precedência codificada pela ORDEM das alternativas
 * (topo = mais alta) e <assoc=right> em `**` e nas atribuições. Veja a Tabela 1
 * da especificação.
 */
grammar JSS;

// ===================== PARSER =====================

program        : topLevelDecl* EOF ;

topLevelDecl   : funcDecl | classDecl | statement ;

// ---- Declarações de variáveis / constantes ----
varDecl        : 'let' type arrayDim? varInit (',' varInit)* ';' ;
varInit        : IDENTIFIER ('=' initializer)? ;
constDecl      : 'const' type arrayDim? IDENTIFIER '=' initializer ';' ;

initializer    : arrayLiteral | expr ;        // '[' só inicia arrayLiteral
arrayDim       : ('[' expr? ']')+ ;           // 1+ dimensões -> vetores multidimensionais
arrayLiteral   : '[' (arrayElem (',' arrayElem)*)? ']' ;
arrayElem      : arrayLiteral | expr ;        // literais aninhados: [[1,2],[3,4]]

type           : 'int' | 'real' | 'str' | 'bool' | IDENTIFIER ; // IDENTIFIER = nome de classe

// ---- Funções ----
funcDecl       : 'function' returnType IDENTIFIER '(' paramList? ')' block ;
returnType     : type arrayDim? | 'void' ;
paramList      : param (',' param)* ;
param          : type arrayDim? IDENTIFIER ;

// ---- Classes ----
classDecl       : 'class' IDENTIFIER '{' attrDecl+ constructorDecl methodDecl* '}' ;
attrDecl        : type arrayDim? IDENTIFIER ';' ;
constructorDecl : IDENTIFIER 'constructor' '(' paramList? ')' block ;
methodDecl      : returnType IDENTIFIER '(' paramList? ')' block ;

// ---- Comandos ----
block          : '{' statement* '}' ;
statement      : varDecl
               | constDecl
               | ifStmt
               | whileStmt
               | forStmt
               | returnStmt
               | breakStmt
               | block
               | exprStmt
               ;

ifStmt         : 'if' '(' expr ')' block elseIf* elseBlock? ;
elseIf         : 'else' 'if' '(' expr ')' block ;
elseBlock      : 'else' block ;
whileStmt      : 'while' '(' expr ')' block ;
forStmt        : 'for' '(' forInit? ';' expr? ';' forUpdate? ')' block ;
forInit        : 'let' type arrayDim? varInit (',' varInit)*   # forInitDecl
               | exprList                                       # forInitExpr
               ;
forUpdate      : exprList ;
exprList       : expr (',' expr)* ;
returnStmt     : 'return' expr? ';' ;
breakStmt      : 'break' ';' ;
exprStmt       : expr ';' ;                    // inclui atribuições: x = 5;

// ---- Expressões (recursiva à esquerda; ordem das alternativas = precedência) ----
expr
    : expr '[' expr ']'                                  # indexExpr    // postfix (mais alta)
    | expr '.' IDENTIFIER ('(' argList? ')')?            # memberExpr   // attr/método; console.log
    | expr '(' argList? ')'                              # callExpr     // f(args)
    | expr op=('++' | '--')                              # postfixExpr  // i++, i--
    | op=('!' | '+' | '-' | '++' | '--') expr           # unaryExpr    // prec 1
    | <assoc=right> expr '**' expr                       # powExpr      // prec 2
    | expr op=('*' | '/' | '%') expr                     # mulExpr      // prec 3
    | expr op=('+' | '-') expr                           # addExpr      // prec 4
    | expr op=('<' | '>' | '<=' | '>=') expr             # relExpr      // prec 5
    | expr op=('==' | '!=') expr                         # eqExpr       // prec 5
    | expr '&&' expr                                     # andExpr      // prec 6
    | expr '||' expr                                     # orExpr       // prec 7
    | <assoc=right>
      expr op=('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**=' | '&&=' | '||=') expr  # assignExpr // prec 8
    | primary                                            # primaryExpr
    ;

primary
    : literal                                            # literalExpr
    | castType '(' expr ')'                              # castExpr     // int(x), real(x), bool(x), str(x)
    | 'new' IDENTIFIER '(' argList? ')'                  # newExpr
    | 'this'                                             # thisExpr
    | IDENTIFIER                                         # idExpr
    | '(' expr ')'                                       # parenExpr
    ;

castType       : 'int' | 'real' | 'bool' | 'str' ;
literal        : INT_LIT | REAL_LIT | STR_LIT | 'true' | 'false' | 'null' ;
argList        : expr (',' expr)* ;

// ===================== LÉXICO =====================

// Palavras reservadas (declaradas ANTES de IDENTIFIER)
LET         : 'let' ;
CONST       : 'const' ;
INT         : 'int' ;
REAL        : 'real' ;
STR         : 'str' ;
BOOL        : 'bool' ;
TRUE        : 'true' ;
FALSE       : 'false' ;
NULL        : 'null' ;
CLASS       : 'class' ;
CONSTRUCTOR : 'constructor' ;
THIS        : 'this' ;
NEW         : 'new' ;
FUNCTION    : 'function' ;
VOID        : 'void' ;
RETURN      : 'return' ;
IF          : 'if' ;
ELSE        : 'else' ;
WHILE       : 'while' ;
FOR         : 'for' ;
BREAK       : 'break' ;

// Operadores (maximal munch resolve o tamanho do casamento)
POW         : '**' ;
INC         : '++' ;
DEC         : '--' ;
ADD_ASSIGN  : '+=' ;
SUB_ASSIGN  : '-=' ;
MUL_ASSIGN  : '*=' ;
DIV_ASSIGN  : '/=' ;
MOD_ASSIGN  : '%=' ;
POW_ASSIGN  : '**=' ;
AND_ASSIGN  : '&&=' ;
OR_ASSIGN   : '||=' ;
EQ          : '==' ;
NEQ         : '!=' ;
LE          : '<=' ;
GE          : '>=' ;
AND         : '&&' ;
OR          : '||' ;
LT          : '<' ;
GT          : '>' ;
NOT         : '!' ;
ASSIGN      : '=' ;
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
PERCENT     : '%' ;
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACE      : '{' ;
RBRACE      : '}' ;
LBRACK      : '[' ;
RBRACK      : ']' ;
SEMI        : ';' ;
COMMA       : ',' ;
DOT         : '.' ;

// Literais
INT_LIT     : DIGIT+ ;
REAL_LIT    : DIGIT+ '.' DIGIT+ EXP? | DIGIT+ EXP ;   // 1.5 ; 10.8E2 ; 1E3
STR_LIT     : '"' (ESC | ~["\\\r\n])* '"' ;

// Identificadores (ASCII, case-sensitive)
IDENTIFIER  : [a-zA-Z_] [a-zA-Z0-9_]* ;

fragment EXP   : [eE] [+-]? DIGIT+ ;
fragment ESC   : '\\' [btnfr"'\\] ;
fragment DIGIT : [0-9] ;

// Comentário de linha e espaços em branco
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
WS           : [ \t\r\n]+ -> skip ;
