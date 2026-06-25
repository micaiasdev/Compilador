// Exemplo válido de JSS: funções, recursão, classe, vetor multidimensional,
// casts, console.log, for e const.
const int LIMITE = 5;

function int fatorial(int n) {
  if (n > 1) {
    return n * fatorial(n - 1);
  } else {
    return 1;
  }
}

class Ponto {
  int x;
  int y;

  Ponto constructor(int a, int b) {
    this.x = a;
    this.y = b;
  }

  int soma() {
    return this.x + this.y;
  }
}

function void main() {
  let int[2][3] matriz = [[1, 2, 3], [4, 5, 6]];
  matriz[1][2] = 0;

  let real media = (1 + 2 + 3) / 3.0;
  let str msg = "media = " + media;

  let Ponto p = new Ponto(10, 20);
  console.log(msg, p.soma(), fatorial(LIMITE));

  for (let int i = 0; i < LIMITE; i += 1) {
    console.log("i =", i);
  }
}
