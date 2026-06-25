// Vetores multidimensionais: declaração com literal aninhado e acesso encadeado.
function void main() {
  let int[2][3] matriz = [[1, 2, 3], [4, 5, 6]];
  matriz[0][0] = 10;
  matriz[1][2] = matriz[0][1] + 5;

  let int soma = 0;
  for (let int i = 0; i < 2; i += 1) {
    for (let int j = 0; j < 3; j += 1) {
      soma += matriz[i][j];
    }
  }
  console.log("soma =", soma);

  let real[2][2] m2 = [[1.0, 2.0], [3.0, 4.0]];
  console.log(m2[1][1]);
}
