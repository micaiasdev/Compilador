// Funções, parâmetros, retorno e recursão.
function int fatorial(int n) {
  if (n > 1) {
    return n * fatorial(n - 1);
  } else {
    return 1;
  }
}

function real media(real x, real y) {
  return (x + y) / 2.0;
}

function void main() {
  let int f = fatorial(5);
  let real m = media(8.0, 9.0);
  console.log("5! =", f, " media =", m);
}
