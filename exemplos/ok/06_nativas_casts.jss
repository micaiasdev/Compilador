// Funções nativas: input, console.log (com vários itens e vazio) e casts.
function void main() {
  let int a;
  let real b;
  let str nome;
  input(a, b, nome);

  console.log("Soma:", a + 10);
  console.log();

  console.log(int(3.9));
  console.log(real(10));
  console.log(bool(0));
  console.log(str(15));
  console.log(str(true));

  let int x = int(3.9) + int(true);
  let real y = real(5) + real(false);
  console.log(x, y);
}
