// Controle de fluxo: if/else if/else, while com break, for nas duas formas.
function void main() {
  let int x = 5;

  if (x > 10) {
    console.log("grande");
  } else if (x > 3) {
    console.log("medio");
  } else {
    console.log("pequeno");
  }

  let int i = 0;
  while (i < 10) {
    i += 1;
    if (i == 5) { break; }
  }

  for (let int j = 0; j < 3; j += 1) {
    console.log("j =", j);
  }

  let int k = 0;
  for (k = 0; k < 2; k += 1) {
    console.log("k =", k);
  }

  for (; ;) {
    break;
  }
}
