# Decisões do Front-end JSS

Este documento registra as decisões adotadas para implementar o front-end do compilador da linguagem Java Script Simplificado (JSS), com base na especificação do trabalho e nas decisões tomadas pelo grupo.

## Execução do Compilador

O compilador deve ler o código-fonte JSS pela entrada padrão do processo.

Exemplo:

```powershell
python compilador.py < programa.jss
```

Também é aceitável redirecionar conteúdo via pipe:

```powershell
Get-Content programa.jss | python compilador.py
```

O compilador não deve depender de abrir diretamente o arquivo pelo caminho recebido como argumento.

Como a leitura é feita pela entrada padrão, a extensão `.jss` fica como convenção de uso e entrega do arquivo-fonte, não como uma validação obrigatória dentro do compilador.

## Saída do Compilador

Em caso de sucesso:

```txt
Linguagem compilada com sucesso
```

Em caso de erro:

```txt
Erro na linha X: descrição do erro
```

O compilador deve parar no primeiro erro encontrado.

Os erros podem ser léxicos, sintáticos ou semânticos.

## Entrada Padrão vs. `input()`

A entrada padrão do compilador é usada para o compilador receber o código-fonte JSS.

A função nativa `input(...)` pertence à linguagem JSS e deve ser apenas reconhecida e validada pelo front-end. O front-end não executa o programa, portanto não executa a leitura de dados em tempo de execução.

Exemplo válido para análise:

```jss
input(a, b, nome);
```

A chamada deve ser aceita somente se os argumentos forem variáveis mutáveis dos tipos:

```txt
int
real
str
```

O tipo `bool` não será aceito em `input(...)`.

## Identificadores

Os identificadores serão restritos a ASCII.

Formato aceito:

```txt
[A-Za-z_][A-Za-z0-9_]*
```

A linguagem é case-sensitive.

Exemplo:

```jss
var
VAR
VaR
```

Esses três nomes representam identificadores diferentes, desde que não sejam palavras reservadas.

## Palavras Reservadas

As palavras reservadas citadas ou necessárias pela especificação são:

```txt
let
const
int
real
str
bool
true
false
null
class
constructor
this
new
function
void
return
if
else
while
for
break
```

`main` não será palavra reservada comum. Ela será tratada como um nome especial de função: se existir, deve ser uma função sem parâmetros.

## Funções Nativas Especiais

As funções nativas serão tratadas como chamadas especiais reconhecidas pelo compilador, não como funções declaradas pelo usuário.

Funções nativas:

```txt
input(...)
console.log(...)
int(...)
real(...)
bool(...)
str(...)
```

`console.log` será tratado como função nativa especial, não como as palavras reservadas separadas `console` e `log`.

As funções `int(...)`, `real(...)`, `bool(...)` e `str(...)` são conversões explícitas. Os mesmos nomes também aparecem como tipos, então o parser deve distinguir pelo contexto.

Exemplo:

```jss
let int x;
x = int(3.9);
```

## Escopo e Shadowing

Será permitido shadowing em escopos internos.

Ao declarar um identificador, o compilador deve verificar apenas o escopo atual.

Ao usar um identificador, o compilador deve procurar do escopo mais interno para o mais externo.

Exemplo válido:

```jss
let int x;

if (true) {
  let int x;
}
```

Exemplo inválido:

```jss
let int x;
let real x;
```

O segundo exemplo é inválido porque redeclara `x` no mesmo escopo.

## Constantes

As declarações com `const` serão aceitas para primitivos, vetores e objetos.

Exemplos válidos:

```jss
const int x = 1;
const real y = 2.5;
const int [3] v = [1, 2, 3];
const Ponto p = new Ponto(1, 2);
```

Uma constante não pode receber nova atribuição após sua declaração.

Vetores constantes não podem ter elementos alterados.

Objetos constantes não podem ter seus atributos alterados.

## Funções

Funções só poderão ser chamadas depois de declaradas.

Exemplo inválido:

```jss
let int x = soma(1, 2);

function int soma(int a, int b) {
  return a + b;
}
```

Nesse caso, o compilador deve retornar erro de função não declarada na linha da chamada.

Não será implementada uma primeira passagem global para registrar todas as funções antes de validar os corpos.

Funções recursivas continuam possíveis, desde que a função esteja em processo de declaração quando a chamada recursiva aparecer dentro de seu próprio corpo.

Exemplo válido:

```jss
function int fatorial(int n) {
  if (n > 1) {
    return n * fatorial(n - 1);
  } else {
    return 1;
  }
}
```

## Função `main`

A função `main` é facultativa.

O front-end deve aceitar programas com ou sem `main`.

Se existir, `main` deve ser uma função sem parâmetros.

Exemplo válido:

```jss
function void main() {
  console.log("ok");
}
```

## Tipos

Tipos primitivos válidos:

```txt
int
real
str
bool
```

O tipo `float` não será aceito. Quando aparecer no exemplo do documento, será tratado como erro da especificação. O tipo correto é `real`.

## Operadores e Tipagem

### Operador `+`

Entre `int` e `int`, retorna `int`.

Entre `real` e `real`, retorna `real`.

Entre `int` e `real`, retorna `real`.

Entre `str` e tipo primitivo, retorna `str`.

Exemplos válidos:

```jss
"idade: " + 10
"valor: " + 3.5
"ativo: " + true
```

`+` entre `str` e objeto ou vetor deve ser rejeitado.

### Operadores `==` e `!=`

Serão permitidos entre operandos do mesmo tipo.

Também serão permitidos entre `int` e `real`, com promoção implícita para `real`.

### Operadores `<`, `>`, `<=` e `>=`

Serão permitidos apenas entre operandos numéricos:

```txt
int
real
```

Comparações relacionais com `str`, `bool`, vetor ou objeto devem ser rejeitadas.

### Operadores `&&` e `||`

Serão permitidos apenas entre operandos `bool`.

O retorno será `bool`.

### Atribuição `=`

O tipo da expressão deve ser compatível com o tipo da variável.

Será permitido atribuir `int` a `real`.

Será rejeitado atribuir `real` a `int` sem cast explícito.

Exemplo válido:

```jss
let real x;
x = 10;
```

Exemplo inválido:

```jss
let int x;
x = 10.5;
```

### Atribuições Compostas

Atribuições compostas devem ser validadas como uma operação seguida de atribuição.

Exemplo:

```jss
x += y;
```

Deve ser validado como:

```jss
x = x + y;
```

### Operadores `++` e `--`

Serão permitidos apenas em variáveis mutáveis dos tipos:

```txt
int
real
```

Devem ser rejeitados em:

- constantes;
- literais;
- expressões;
- vetores como entidade completa;
- objetos;
- atributos de objetos constantes.

### Operador `/`

`int / int` retorna `int`.

Se pelo menos um operando for `real`, o retorno será `real`.

### Operador `%`

Permitido apenas como:

```txt
int % int
```

O retorno será `int`.

### Operador `**`

Permitido apenas como:

```txt
int ** int
```

O retorno será `int`.

## `console.log(...)`

`console.log(...)` deve aceitar expressões dos tipos:

```txt
int
real
str
bool
```

A lista de expressões pode ser vazia.

Exemplo válido:

```jss
console.log();
```

Nesse caso, a chamada representa apenas uma quebra de linha.

## `for`

No documento, o `*` usado na sintaxe do `for` indica que as partes de declaração, atribuição e expressão lógica podem ser vazias.

O separador entre as três partes do cabeçalho será `;`.

Exemplos estruturalmente válidos:

```jss
for (; ; ) {
}
```

```jss
for (i = 0; i < 10; i += 1) {
}
```

```jss
for (let int i = 0; i < 10; i += 1) {
}
```

## Classes

Classes serão suportadas, mesmo que a seção de programa do documento não as mencione explicitamente.

A semântica completa de classes ainda depende de investigação adicional.

Pontos ainda pendentes:

- chamada de métodos;
- acesso e atribuição de atributos;
- uso de `this`;
- escopo de atributos;
- ordem de declaração de classes;
- compatibilidade entre `null` e objetos;
- regras exatas para objetos constantes.

