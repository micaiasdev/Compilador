// Erro semântico: função chamada antes de ser declarada.
function void main() {
  let int r = soma(1, 2);
}

function int soma(int a, int b) {
  return a + b;
}
