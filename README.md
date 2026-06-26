# Compilador JSS - Front-end

Front-end do compilador da linguagem **Java Script Simplificado (JSS)**,
projeto final da disciplina de Compiladores (UFPI, 2026.1).

O compilador lê o código-fonte JSS pela **entrada padrão** e imprime na
**saída padrão**:

- `Linguagem compilada com sucesso`, quando o programa é válido; ou
- uma ou mais linhas `Erro na linha X: descrição do erro`, quando existem erros
  léxicos, sintáticos ou semânticos.

Implementado em **Java 17+** com **ANTLR 4**. O ANTLR não precisa ser instalado
separadamente: o Maven baixa o plugin e o runtime automaticamente.

---

## 1. Pré-requisitos

| Ferramenta | Versão | Uso |
|---|---:|---|
| JDK | 17 ou superior | compilar e executar |
| Apache Maven | 3.8+ | gerar parser ANTLR e empacotar o `.jar` |

Verifique no terminal:

```powershell
java -version
mvn -version
```

Se `java` funcionar, mas `mvn` mostrar algo como:

```text
O termo 'mvn' não é reconhecido...
```

então o Maven não está instalado corretamente ou não foi adicionado ao `PATH`.

---

## 2. Instalar Maven no Windows

### Opção A: instalação manual

1. Baixe o **Binary zip archive** em:
   <https://maven.apache.org/download.cgi>

2. Extraia para uma pasta simples, por exemplo:

```text
C:\apache-maven-3.9.11
```

3. Adicione a pasta `bin` ao `Path` do Windows:

```text
C:\apache-maven-3.9.11\bin
```

Caminho no Windows:

```text
Menu Iniciar
> Editar variáveis de ambiente do sistema
> Variáveis de Ambiente
> Path
> Editar
> Novo
> C:\apache-maven-3.9.11\bin
```

4. Feche e abra novamente o PowerShell/VS Code.

5. Teste:

```powershell
mvn -version
```

### Opção B: instalador/gerenciador de pacotes

Se o `winget` estiver disponível:

```powershell
winget install Apache.Maven
```

Depois feche e reabra o terminal e teste:

```powershell
mvn -version
```

> Instalar a extensão "Maven for Java" no VS Code **não instala** o comando
> `mvn`. Ela só integra projetos Maven ao editor.

---

## 3. Build do projeto

Na pasta raiz do projeto, onde fica o `pom.xml`, rode:

```powershell
mvn clean package
```

Esse comando:

- baixa as dependências;
- gera o lexer/parser a partir de `src/main/antlr4/br/ufpi/jss/JSS.g4`;
- compila o código Java;
- gera o executável:

```text
target\jss-compiler.jar
```

Se o VS Code continuar mostrando imports do ANTLR em vermelho, rode:

```powershell
mvn clean generate-sources
```

Depois use:

```text
Ctrl+Shift+P > Java: Clean Java Language Server Workspace
```

e recarregue o VS Code.

---

## 4. Executar um arquivo JSS

O compilador lê pela entrada padrão. No PowerShell, use `Get-Content -Raw`:

```powershell
Get-Content caminho\arquivo.jss -Raw | java -jar target\jss-compiler.jar
```

Exemplo:

```powershell
Get-Content exemplos\ok\02_funcoes_recursao.jss -Raw | java -jar target\jss-compiler.jar
```

Saída esperada:

```text
Linguagem compilada com sucesso
```

Para testar um arquivo próprio na raiz do projeto:

```powershell
Get-Content .\meu_teste.jss -Raw | java -jar target\jss-compiler.jar
```

No `cmd`, o redirecionamento clássico também funciona:

```bat
java -jar target\jss-compiler.jar < programa.jss
```

No Linux/macOS:

```bash
java -jar target/jss-compiler.jar < programa.jss
```

---

## 5. Rodar a suíte de testes

Primeiro gere o `.jar`:

```powershell
mvn clean package
```

Depois rode:

```powershell
.\rodar_testes.ps1
```

O script verifica:

- `exemplos\ok\*.jss`: devem compilar com sucesso;
- `exemplos\erros\*.jss`: devem gerar uma ou mais mensagens `Erro na linha...`.

Saída esperada ao final:

```text
RESULTADO: N OK, 0 falha(s)
```

Se o PowerShell bloquear o script com política de execução, use:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\rodar_testes.ps1
```

Isso libera apenas essa execução, sem mudar a política do sistema.

---

## 6. Testar arquivo por arquivo

Além da suíte, você pode rodar qualquer caso isolado:

```powershell
Get-Content testes\1_basics.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\2_operators.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\3_control_flow.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\5_classes.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\6_functions.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\7_errors.jss -Raw | java -jar target\jss-compiler.jar
Get-Content testes\8_erros_funcao.jss -Raw | java -jar target\jss-compiler.jar
```

Arquivos válidos devem imprimir:

```text
Linguagem compilada com sucesso
```

Arquivos com erro podem imprimir várias linhas:

```text
Erro na linha 6: identificador 'y' nao declarado
Erro na linha 9: identificador 'x' ja declarado neste escopo
```

Observação importante: se houver erro léxico ou sintático, a análise semântica é
pulada. Isso evita mensagens falsas sobre uma árvore sintática incompleta.

---

## 7. Erros comuns e como resolver

### `mvn` não é reconhecido

O Maven não está no `PATH`. Instale o Maven e adicione:

```text
C:\apache-maven-3.9.11\bin
```

ao `Path` do Windows. Depois feche e reabra o terminal.

### VS Code não reconhece `org.antlr.v4.runtime`

Rode:

```powershell
mvn clean generate-sources
```

Depois:

```text
Ctrl+Shift+P > Java: Clean Java Language Server Workspace
```

### `.\rodar_testes.ps1` foi bloqueado

Use:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\rodar_testes.ps1
```

### Alterei a gramática, mas o teste continua igual

Depois de alterar `JSS.g4`, gere novamente o parser e o `.jar`:

```powershell
mvn clean package
```

Rodar só `java -jar target\jss-compiler.jar` usa o `.jar` já existente. Se ele
não foi reconstruído, suas mudanças ainda não entraram.

### `i` não declarado dentro de `for`

Este é um erro **semântico**, não sintático. Exemplo:

```jss
function void main() {
  for (i = 0; i < 10; i++) {
    console.log(i);
  }
}
```

O parser aceita a forma da expressão, mas a análise semântica deve acusar:

```text
Erro na linha X: identificador 'i' nao declarado
```

---

## 8. Estrutura do projeto

```text
.
├── pom.xml
├── README.md
├── rodar_testes.ps1
├── src/main/antlr4/br/ufpi/jss/
│   └── JSS.g4
├── src/main/java/br/ufpi/jss/
│   ├── Main.java
│   ├── Compilador.java
│   ├── erro/
│   ├── tipos/
│   ├── simbolos/
│   ├── semantico/
│   └── codegen/
├── exemplos/
│   ├── ok/
│   └── erros/
└── testes/
```

---

## 9. Resumo da linguagem JSS

- Tipos primitivos: `int`, `real`, `str`, `bool`.
- Tipos derivados: vetores e classes.
- Declarações: `let` e `const`.
- Funções: `function <tipo|void> nome(params) { ... }`.
- `main` é opcional e, se existir, não pode ter parâmetros.
- Controle de fluxo: `if`, `else if`, `else`, `while`, `for`, `break`.
- Nativas: `input(...)`, `console.log(...)`.
- Casts: `int(...)`, `real(...)`, `bool(...)`, `str(...)`.
- Comentários: apenas comentários de linha com `//`.

---

## 10. Back-end

O front-end já produz informações semânticas úteis para uma etapa futura de
geração de código. O esqueleto do gerador Jasmin está em:

```text
src/main/java/br/ufpi/jss/codegen/GeradorJasmin.java
```
