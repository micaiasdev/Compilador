// Operadores: precedência, promoção numérica e concatenação de strings.
function void main() {
  let int a = 2 + 3 * 4;       // 14
  let int b = 2 ** 3 ** 2;     // associa à direita: 2 ** 9 = 512
  let real c = 10 / 4 + 1.5;   // 10/4 = 2 (int) ; 2 + 1.5 = 3.5
  let int d = 17 % 5;          // 2
  let bool e = (a > b) || (c < 5.0) && !false;
  let str s = "a=" + a + " ok=" + true + " c=" + c;
  console.log(a, b, c, d, e, s);
}
