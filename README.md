# Compilador JSS — Front-end (Java Script Simplificado)

Front-end (análise léxica, sintática e semântica) do compilador da linguagem
**Java Script Simplificado (JSS)**, projeto final da disciplina de Compiladores
(UFPI, 2026.1).

O compilador lê o código-fonte JSS pela **entrada padrão**, e imprime na **saída
padrão**:

- `Linguagem compilada com sucesso` — quando o programa é válido; ou
- `Erro na linha X: descrição do erro` — no **primeiro** erro encontrado
  (léxico, sintático ou semântico).

Implementado em **Java 17** com **ANTLR 4** (gramática combinada → lexer/parser
gerados) e análise semântica via *visitor*.

---

## 1. Pré-requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| **JDK** | 17 ou superior | compilar e executar |
| **Apache Maven** | 3.8+ | build e geração do parser (ANTLR) |

> O ANTLR **não** precisa ser instalado: ele é baixado automaticamente pelo
> Maven (`antlr4-maven-plugin`) e o *runtime* fica embutido no `.jar` final.
> Para apenas **executar** o `.jar` já construído, basta o **JRE 17+**.

Verifique a instalação:

```powershell
java -version
mvn -version
```

---

## 2. Instalação / Build

Na pasta do projeto, gere o executável (um *fat jar* com tudo embutido):

```powershell
mvn clean package
```

Isso produz **`target/jss-compiler.jar`**. O Maven, nessa etapa, também gera o
lexer/parser a partir da gramática `src/main/antlr4/br/ufpi/jss/JSS.g4`.

---

## 3. Execução

O programa-fonte é lido da **entrada padrão**. Como o **PowerShell não suporta**
o operador de redirecionamento `<`, use uma das formas abaixo.

**PowerShell** (use `-Raw`):

```powershell
Get-Content programa.jss -Raw | java -jar target\jss-compiler.jar
```

**Prompt de Comando (cmd)** — redirecionamento clássico, igual ao da especificação:

```bat
java -jar target\jss-compiler.jar < programa.jss
```

**Linux / macOS (bash):**

```bash
java -jar target/jss-compiler.jar < programa.jss
```

A saída é sempre em **UTF-8** (acentos corretos), e um eventual *BOM* no início
do arquivo é ignorado automaticamente. O processo encerra com código **0** em
caso de sucesso e **1** em caso de erro.

---

## 4. Exemplos

### 4.1. Programa válido

Arquivo [`exemplos/ok/02_funcoes_recursao.jss`](exemplos/ok/02_funcoes_recursao.jss):

```jss
function int fatorial(int n) {
  if (n > 1) {
    return n * fatorial(n - 1);
  } else {
    return 1;
  }
}

function void main() {
  let int f = fatorial(5);
  console.log("5! =", f);
}
```

```powershell
Get-Content exemplos\ok\02_funcoes_recursao.jss -Raw | java -jar target\jss-compiler.jar
```

Saída:

```
Linguagem compilada com sucesso
```

### 4.2. Programa com erro

Arquivo [`exemplos/erros/02_real_para_int.jss`](exemplos/erros/02_real_para_int.jss):

```jss
// Erro semântico: atribuir real a int sem cast explícito.
function void main() {
  let int x;
  x = 10.5;
}
```

```powershell
Get-Content exemplos\erros\02_real_para_int.jss -Raw | java -jar target\jss-compiler.jar
```

Saída:

```
Erro na linha 4: não é possível atribuir real a int
```

Mais exemplos prontos estão em **`exemplos/ok/`** (válidos) e
**`exemplos/erros/`** (um erro por arquivo: redeclaração, `real`→`int`, função
não declarada, `%`/`**` com real, relacional com `str`, `++` em constante,
`input(bool)`, tipo `float`, literal de vetor incompatível, excesso de índices,
`main` com parâmetro, `break` fora de laço, erro sintático e erro léxico).

---

## 5. Rodar a suíte de testes

O script verifica todos os exemplos de uma vez (cada `ok/` deve compilar e cada
`erros/` deve falhar):

```powershell
.\rodar_testes.ps1
```

Saída resumida ao final: `RESULTADO: N OK, 0 falha(s)`.

---

## 6. Estrutura do projeto

```
.
├── pom.xml                              # build Maven (ANTLR + fat jar)
├── README.md
├── rodar_testes.ps1                     # executa a suíte de exemplos
├── src/main/antlr4/br/ufpi/jss/
│   └── JSS.g4                           # gramática (léxico + sintaxe)
├── src/main/java/br/ufpi/jss/
│   ├── Main.java                        # entrada (stdin -> resultado)
│   ├── Compilador.java                  # pipeline: léxico -> sintaxe -> semântica
│   ├── erro/                            # ErroCompilacao, OuvinteErroSintatico
│   ├── tipos/                           # modelo de tipos (Tipo, TipoVetor, ...)
│   ├── simbolos/                        # tabela de símbolos e escopos
│   ├── semantico/                       # AnalisadorSemantico, regras, nativas
│   └── codegen/                         # GeradorJasmin (esqueleto do back-end)
└── exemplos/
    ├── ok/                              # programas válidos
    └── erros/                           # programas com erro
```

---

## 7. Resumo da linguagem JSS

- **Tipos primitivos:** `int`, `real`, `str`, `bool`. **Derivados:** vetores
  (inclusive **multidimensionais**, ex.: `let int[2][3] m = [[1,2,3],[4,5,6]];`)
  e **classes** (`class`, `constructor`, `this`, `new`).
- **Declarações:** `let` (variável) e `const` (constante). Identificadores:
  `[A-Za-z_][A-Za-z0-9_]*`, *case-sensitive*.
- **Funções:** `function <tipo|void> nome(params) { ... }`; só podem ser
  chamadas **após** declaradas; recursão é permitida. `main` é **opcional** e,
  se existir, não tem parâmetros.
- **Controle de fluxo:** `if/else if/else`, `while`, `for` (com `break`).
- **Nativas:** `input(...)`, `console.log(...)` e *casts* `int/real/bool/str(...)`.
- **Comentários:** apenas de linha, com `//`.

---

## 8. Back-end (etapa futura)

O front-end já expõe, ao final da análise, uma **árvore anotada com os tipos** e
as tabelas de **funções/classes** (classe `InfoSemantica`). O gerador de código
intermediário **Jasmin** (`codegen/GeradorJasmin`) é um esqueleto preparado para
consumir essa informação: emitirá um arquivo `.j`, montado depois com
`java -jar jasmin.jar` para gerar um `.class` executável na JVM.
