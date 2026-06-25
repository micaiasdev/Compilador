// Classes: atributos, construtor, this, métodos, new e acesso a membros.
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

  Ponto clonar() {
    return new Ponto(this.x, this.y);
  }
}

function void main() {
  let Ponto p = new Ponto(3, 4);
  console.log("soma =", p.soma());
  console.log("x =", p.x);

  let Ponto q = p.clonar();
  q = null;
}
