import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import java.util.Arrays;
import java.util.List;

public class SubcadeiaDeSomaMaxima {

    public static List<Integer> forcaBruta(int... a) {
        int somaMaxima = 0;
        int i = -1, j = -1;
        int n = a.length;
        for (int primeiro = 0; primeiro < n; primeiro++) {
            for (int ultimo = primeiro; ultimo < n; ultimo++) {
                int soma = 0;
                for (int k = primeiro; k <= ultimo; k++) {
                    soma = soma + a[k];
                }
                if (soma > somaMaxima) {
                    somaMaxima = soma;
                    i = primeiro;
                    j = ultimo;
                }
            }
        }
        return Arrays.asList(somaMaxima, i, j);
    }

    public static List<Integer> versaoMelhorada(int... a) {
        int somaMaxima = 0;
        int i = -1, j = -1;
        int n = a.length;
        for (int primeiro = 0; primeiro < n; primeiro++) {
            int soma = 0;
            for (int ultimo = primeiro; ultimo < n; ultimo++) {
                soma = soma + a[ultimo];
                if (soma > somaMaxima) {
                    somaMaxima = soma;
                    i = primeiro;
                    j = ultimo;
                }
            }
        }
        return Arrays.asList(somaMaxima, i, j);
    }

    public static List<Integer> dividirEConquistar(int... a) {
        return dividirEConquistar(a, 0, a.length - 1);
    }

    public static List<Integer> dividirEConquistar(int[] a, int esquerda, int direita) {
        if (esquerda == direita) {
            if (a[esquerda] > 0) {
                return Arrays.asList(a[esquerda], esquerda, direita);
            } else {
                return Arrays.asList(0, esquerda, direita);
            }
        }
        int meio = (esquerda + direita) / 2;
        int somaMaximaDaEsquerda = 0;
        int soma = 0;
        int i = -1;
        for (int k = meio; k >= esquerda; k--) {
            soma = soma + a[k];
            if (soma > somaMaximaDaEsquerda) {
                somaMaximaDaEsquerda = soma;
                i = k;
            }
        }
        int somaMaximaDaDireita = 0;
        soma = 0;
        int j = -1;
        for (int k = meio + 1; k <= direita; k++) {
            soma = soma + a[k];
            if (soma > somaMaximaDaDireita) {
                somaMaximaDaDireita = soma;
                j = k;
            }
        }
        List<Integer> resultadoDaSomaMaximaDoCentro = Arrays.asList(somaMaximaDaEsquerda + somaMaximaDaDireita, i, j);
        List<Integer> resultadoDaSomaMaximaDoPrefixo = dividirEConquistar(a, esquerda, meio);
        List<Integer> resultadoDaSomaMaximaDoSufixo = dividirEConquistar(a, meio + 1, direita);
        if (resultadoDaSomaMaximaDoPrefixo.get(0) >= resultadoDaSomaMaximaDoSufixo.get(0)) {
            if (resultadoDaSomaMaximaDoPrefixo.get(0) >= resultadoDaSomaMaximaDoCentro.get(0)) {
                return resultadoDaSomaMaximaDoPrefixo;
            } else {
                return resultadoDaSomaMaximaDoCentro;
            }
        } else {
            if (resultadoDaSomaMaximaDoSufixo.get(0) >= resultadoDaSomaMaximaDoCentro.get(0)) {
                return resultadoDaSomaMaximaDoSufixo;
            } else {
                return resultadoDaSomaMaximaDoCentro;
            }
        }
    }

    public static List<Integer> programacaoDinamica(int... a) {
        int primeiro = 0;
        int soma = 0;
        int somaMaxima = 0;
        int i = 0;
        int j = 0;
        int n = a.length;
        int ultimo;
        for (ultimo = 0; ultimo < n; ultimo++) {
            soma = soma + a[ultimo];
            if (soma > somaMaxima) {
                somaMaxima = soma;
                i = primeiro;
                j = ultimo;
            } else if (soma < 0) {
                primeiro = ultimo + 1;
                soma = 0;
            }
        }
        return Arrays.asList(somaMaxima, i, j);
    }

    public static void main(String[] args) {
        assertThat(forcaBruta(-2, 11, -4, 13, -5, -2), hasItems(20, 1, 3));
        assertThat(versaoMelhorada(-2, 11, -4, 13, -5, -2), hasItems(20, 1, 3));
        assertThat(dividirEConquistar(-2, 11, -4, 13, -5, -2), hasItems(20, 1, 3));
        assertThat(programacaoDinamica(-2, 11, -4, 13, -5, -2), hasItems(20, 1, 3));
    }
}
