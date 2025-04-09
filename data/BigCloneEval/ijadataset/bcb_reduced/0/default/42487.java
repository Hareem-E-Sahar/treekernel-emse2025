import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * Analiza ciertos numeros irracionales.
 * @author Moises Osorio
 */
public class IrrationalAnalysis {

    private static BigDecimal TWO = new BigDecimal(2);

    private int number;

    private IrrationalType type;

    private int precision;

    private BigDecimal irrational;

    public IrrationalAnalysis(IrrationalType type, int precision) {
        this.type = type;
        this.precision = precision;
        calculateDigits();
    }

    public IrrationalAnalysis(int number, int precision) {
        type = IrrationalType.SQUARED_ROOT;
        this.number = number;
        this.precision = precision;
        calculateDigits();
    }

    /**
	 * Obtiene la parte fraccionaria del numero en cierta base.
	 * @param base
	 * @return
	 */
    private String getFractionalPart(int base) {
        final BigDecimal BASE = new BigDecimal(base);
        String rep = "";
        BigDecimal fraction = irrational;
        fraction = fraction.subtract(new BigDecimal(fraction.intValue()));
        for (int i = 0; i < precision; i++) {
            fraction = fraction.multiply(BASE);
            int digit = fraction.intValue();
            rep += Integer.toString(digit, base);
            fraction = fraction.subtract(new BigDecimal(digit));
        }
        return rep;
    }

    /**
	 * Imprime las estadisticas en cierta base sobre el archivo indicado.
	 * @param base
	 * @param file
	 * @throws IOException
	 */
    public void printFullStatistics(int base, String file) throws IOException {
        PrintWriter out = new PrintWriter(file);
        out.println(getFractionalPart(base));
        out.println();
        for (int partitions = 1; partitions <= 1 << 3; partitions <<= 1) {
            printStatistics(partitions, base, out);
            out.println();
        }
        out.flush();
        out.close();
    }

    /**
	 * Imprime las estadisticas para cierto numero de particiones en cierto archivo dada una base.
	 * @param partitions
	 * @param base
	 * @param file
	 * @throws IOException
	 */
    public void printStatistics(int partitions, int base, String file) throws IOException {
        PrintWriter out = new PrintWriter(file);
        printStatistics(partitions, base, out);
        out.flush();
        out.close();
    }

    /**
	 * Imprime la estadisticas en el flujo indicado.
	 * @param partitions
	 * @param base
	 * @param out
	 */
    private void printStatistics(int partitions, int base, PrintWriter out) {
        out.print("Dígito/Rango");
        int partSize = precision / partitions;
        for (int j = 0; j < partitions; j++) out.print("," + (j * partSize + 1) + ".." + ((j + 1) * partSize));
        out.println();
        int[][] stats = getStatistics(partitions, base);
        for (int i = 0; i < base; i++) {
            out.print(i);
            for (int j = 0; j < partitions; j++) out.print("," + stats[j][i]);
            out.println();
        }
    }

    /**
	 * Obtiene las estadisticas para cierto numero de particiones y base.
	 * @param partitions
	 * @param base
	 * @return
	 */
    public int[][] getStatistics(int partitions, int base) {
        String rep = getFractionalPart(base);
        int partSize = rep.length() / partitions;
        int[][] stats = new int[partitions][base];
        for (int i = 0; i < partitions; i++) for (int pos = i * partSize; pos < (i + 1) * partSize; pos++) {
            int value = Integer.parseInt(rep.charAt(pos) + "", base);
            stats[i][value]++;
        }
        return stats;
    }

    /**
	 * Realiza el calculo del numero irracional.
	 */
    private void calculateDigits() {
        if (type == IrrationalType.PI) irrational = calculatePI(); else if (type == IrrationalType.GOLDEN) irrational = calculateGolden(); else irrational = calculateSquaredRoot(number);
    }

    /**
	 * Obtiene el maximo comun divisor.
	 * @param a
	 * @param b
	 * @return
	 */
    private long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    /**
	 * Obtiene el minimo comun multiplo.
	 * @param a
	 * @param b
	 * @return
	 */
    private long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }

    /**
	 * Realiza el calculo de PI.
	 * @return
	 */
    private BigDecimal calculatePI() {
        final BigDecimal SIXTEEN = new BigDecimal(16);
        BigDecimal pi = BigDecimal.ZERO;
        BigDecimal den = BigDecimal.ONE;
        for (int i = 0; i < precision; i++) {
            int a = 8 * i + 1;
            int b = 8 * i + 4;
            int c = 8 * i + 5;
            int d = 8 * i + 6;
            long lcm = lcm(lcm(lcm(a, b), c), d);
            long value = 4 * lcm / a - 2 * lcm / b - lcm / c - lcm / d;
            BigDecimal divisor = den.multiply(new BigDecimal(lcm));
            pi = pi.add(new BigDecimal(value).divide(divisor, precision, BigDecimal.ROUND_HALF_UP));
            den = den.multiply(SIXTEEN);
        }
        return pi;
    }

    /**
	 * Calcula el valor de la razon dorada.
	 * @return
	 */
    private BigDecimal calculateGolden() {
        BigDecimal root = calculateSquaredRoot(5);
        root = root.add(BigDecimal.ONE).divide(TWO);
        return root;
    }

    /**
	 * Calcula la raiz cuadrada de un numero entero.
	 * @param n
	 * @return
	 */
    private BigDecimal calculateSquaredRoot(int n) {
        final BigDecimal N = new BigDecimal(n);
        BigDecimal root = new BigDecimal(n / 2.0);
        int p = 1;
        while (p < precision) {
            p <<= 1;
            root = root.add(N.divide(root, precision, BigDecimal.ROUND_HALF_UP)).divide(TWO);
        }
        return root;
    }

    /**
	 * Tipos de irracionales contemplados para su calculo y analisis.
	 * @author Moises Osorio
	 */
    static enum IrrationalType {

        SQUARED_ROOT, PI, GOLDEN
    }

    public static void main(String[] args) throws IOException {
        final int PRECISION = 1024;
        IrrationalAnalysis pi = new IrrationalAnalysis(IrrationalType.PI, PRECISION);
        pi.printFullStatistics(2, "pi_2.data");
        pi.printFullStatistics(10, "pi_10.data");
        IrrationalAnalysis golden = new IrrationalAnalysis(IrrationalType.GOLDEN, PRECISION);
        golden.printFullStatistics(2, "golden_2.data");
        golden.printFullStatistics(10, "golden_10.data");
        IrrationalAnalysis sqrt2 = new IrrationalAnalysis(2, PRECISION);
        sqrt2.printFullStatistics(2, "sqrt2_2.data");
        sqrt2.printFullStatistics(10, "sqrt2_10.data");
        IrrationalAnalysis sqrt3 = new IrrationalAnalysis(3, PRECISION);
        sqrt3.printFullStatistics(2, "sqrt3_2.data");
        sqrt3.printFullStatistics(10, "sqrt3_10.data");
        IrrationalAnalysis sqrt5 = new IrrationalAnalysis(3, PRECISION);
        sqrt5.printFullStatistics(2, "sqrt5_2.data");
        sqrt5.printFullStatistics(10, "sqrt5_10.data");
    }
}
