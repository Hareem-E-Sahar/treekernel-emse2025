package matriz;

/**
 *
 * @author rudieri
 */
public class Matrizes {

    double[][] matriz;

    public Matrizes() {
    }

    public double[][] calcularInversa(double[][] matriz) {
        if (matriz.length != matriz[0].length) {
            System.err.println("Precisa ser uma matriz NxN para calcular a Inversa!!!");
            return null;
        }
        double det = getDeterminante(matriz);
        if ((det) == 0) {
            System.err.println("Determinante é igual 0!");
            return null;
        }
        return multiplicarPorConstante(getAdjunta(matriz), (1d / det));
    }

    public void printMatriz(double[][] matriz) {
        if (matriz == null) {
            return;
        }
        System.out.println("Matriz: ");
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                System.out.print(matriz[i][j] + "    ");
            }
            System.out.println("");
        }
    }

    public double[][] multiplicarPorConstante(double[][] matriz, double c) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz.length; j++) {
                matriz[i][j] *= c;
            }
        }
        return matriz;
    }

    public double[][] multiplicar(double[][] m1, double[][] m2) {
        final int m1Linha = m1.length;
        final int m1Coluna = m1[0].length;
        final int m2Linha = m2.length;
        final int m2Coluna = m2[0].length;
        if (m1Coluna != m2Linha) {
            System.err.println("Para multiplicar matrizes, elas precisam ser A[i,x]*B[x,k].");
            System.err.println("E essas é são [" + m1Linha + "," + m1Coluna + "]" + "[" + m2Linha + "," + m2Coluna + "]");
        }
        double[][] mr = new double[m1Linha][m2Coluna];
        for (int i = 0; i < m1Linha; i++) {
            for (int j = 0; j < m2Coluna; j++) {
                for (int k = 0; k < m2Linha; k++) {
                    mr[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mr;
    }

    /**
    Calcula e retorna o determinante de uma matriz.
    @param matriz Uma matriz quadratica de ordem n 
    @return O determinante dessa matriz.
     */
    public double getDeterminante(double[][] matriz) {
        if (matriz.length == 1) {
            if (matriz[0].length == 1) {
                return matriz[0][0];
            }
        }
        double dPrin = 1;
        double dSec = 1;
        for (int i = 0; i < matriz.length; i++) {
            dPrin *= matriz[i][i];
            dSec *= matriz[i][matriz.length - 1 - i];
        }
        return dPrin - dSec;
    }

    /**
    Usa o método de eliminação para calcularInversa o "menor complentar" 
    (não sei bem ao certo o nome) do elemento A[i][j].<br/>
    A linha i e a coluna j serão excluídas e será calculado o determinante dessa nova matriz. 
    @param matriz Uma matriz quadratica de ordem n 
    @param i Linha do elemento que se deseja calcularInversa.
    @param j Coluna do elemento que se deseja calcularInversa.
    @return O menor complentar do elemento A[i,j] da matriz.
     */
    protected double getMenorComplementar(double[][] matriz, double i, double j) {
        double[][] nova = new double[matriz.length - 1][matriz.length - 1];
        int x = 0;
        for (int k = 0; k < matriz.length; k++) {
            if (k != i) {
                int y = 0;
                for (int l = 0; l < matriz.length; l++) {
                    if (l != j) {
                        nova[k - x][l - y] = matriz[k][l];
                    } else {
                        y++;
                    }
                }
            } else {
                x++;
            }
        }
        return getDeterminante(nova);
    }

    /**
    Calcula e retorna a matriz Adjunta usando o coofator.
    @param matriz Uma matriz quadratica de ordem n 
    @return Uma matriz adjunta.
     */
    protected double[][] getAdjunta(double[][] matriz) {
        int size = matriz.length;
        double[][] nova = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                nova[i][j] = getCofator(matriz, i, j);
            }
        }
        return getTransposta(nova);
    }

    /**
    Calcula e retorna o Cofator de um elemento da matriz.
    
    @param matriz Uma matriz quadratica
    @param i Linha do elemento que será calculado o cofator
    @param j Coluna do elemento que será calculado o cofator
     */
    protected double getCofator(double[][] matriz, int i, int j) {
        return (double) (Math.pow(-1, i + j) * getMenorComplementar(matriz, i, j));
    }

    /**
    As linhas viram colunas e as colunas viram linhas.
    @param matriz Uma matriz quadratica
    @return A sua transposta.
     */
    public double[][] getTransposta(double[][] matriz) {
        double[][] nova = new double[matriz.length][matriz.length];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz.length; j++) {
                nova[i][j] = matriz[j][i];
            }
        }
        return nova;
    }

    /**
    xxx
     */
    public static void main(String[] args) {
        double[][] matriz = { { 1, 2 }, { -4, -3 } };
        Matrizes jInv = new Matrizes();
        jInv.calcularInversa(matriz);
    }
}
