# Executa toda a suíte de exemplos JSS e verifica o resultado esperado:
#   - exemplos/ok/*.jss    -> deve imprimir "Linguagem compilada com sucesso"
#   - exemplos/erros/*.jss -> deve imprimir "Erro na linha X: ..."
#
# Uso:  .\rodar_testes.ps1     (rode 'mvn clean package' antes)

Set-Location $PSScriptRoot
$jar = "target\jss-compiler.jar"

if (-not (Test-Path $jar)) {
    Write-Host "Jar não encontrado em $jar. Rode 'mvn clean package' primeiro." -ForegroundColor Yellow
    exit 1
}

$ok = 0
$falhas = 0

function Compilar($caminho) {
    $linhas = @(Get-Content $caminho -Raw | java -jar $jar)
    return ($linhas -join [Environment]::NewLine)
}

function ResumoSaida($saida) {
    return (($saida -split "\r?\n") -join " | ")
}

function EhErroCompilacao($saida) {
    $linhas = @($saida -split "\r?\n" | Where-Object { $_ -ne "" })
    if ($linhas.Count -eq 0) {
        return $false
    }
    return @( $linhas | Where-Object { $_ -notlike "Erro na linha*" } ).Count -eq 0
}

function ContarErros($saida) {
    return @( $saida -split "\r?\n" | Where-Object { $_ -like "Erro na linha*" } ).Count
}

function MinimoErrosEsperados($nomeArquivo) {
    if ($nomeArquivo -like "16_multiplos_*" -or $nomeArquivo -like "17_multiplos_*") {
        return 2
    }
    return 1
}

Write-Host "== Casos validos (exemplos/ok) ==" -ForegroundColor Cyan
foreach ($arquivo in Get-ChildItem "exemplos\ok\*.jss" | Sort-Object Name) {
    $saida = Compilar $arquivo.FullName
    if ($saida -eq "Linguagem compilada com sucesso") {
        Write-Host ("  [OK]    {0}" -f $arquivo.Name)
        $ok++
    } else {
        Write-Host ("  [FALHA] {0} -> {1}" -f $arquivo.Name, (ResumoSaida $saida)) -ForegroundColor Red
        $falhas++
    }
}

Write-Host "== Casos de erro (exemplos/erros) ==" -ForegroundColor Cyan
foreach ($arquivo in Get-ChildItem "exemplos\erros\*.jss" | Sort-Object Name) {
    $saida = Compilar $arquivo.FullName
    $minimo = MinimoErrosEsperados $arquivo.Name
    if ((EhErroCompilacao $saida) -and ((ContarErros $saida) -ge $minimo)) {
        Write-Host ("  [OK]    {0} -> {1}" -f $arquivo.Name, (ResumoSaida $saida))
        $ok++
    } else {
        Write-Host ("  [FALHA] {0} -> {1}" -f $arquivo.Name, (ResumoSaida $saida)) -ForegroundColor Red
        $falhas++
    }
}

Write-Host ""
Write-Host ("RESULTADO: {0} OK, {1} falha(s)" -f $ok, $falhas)
if ($falhas -gt 0) { exit 1 } else { exit 0 }
