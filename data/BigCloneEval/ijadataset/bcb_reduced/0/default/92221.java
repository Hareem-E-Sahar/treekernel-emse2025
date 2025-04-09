import java.util.StringTokenizer;

class P11360_39643 {

    public static void main(String[] args) {
        int cases = Integer.parseInt(readLn());
        for (int k = 0; k < cases; k++) {
            int n = Integer.parseInt(readLn());
            int[][] matrix = new int[n][n];
            for (int j = 0; j < n; j++) {
                String line = readLn();
                for (int l = 0; l < n; l++) {
                    matrix[j][l] = line.charAt(l) - '0';
                }
            }
            int ops = Integer.parseInt(readLn());
            for (int j = 0; j < ops; j++) {
                StringTokenizer st = new StringTokenizer(readLn());
                String op = st.nextToken();
                if ("transpose".equals(op)) {
                    matrix = transpose(matrix);
                } else if ("row".equals(op)) {
                    int a = Integer.parseInt(st.nextToken());
                    int b = Integer.parseInt(st.nextToken());
                    row(matrix, a, b);
                } else if ("col".equals(op)) {
                    int a = Integer.parseInt(st.nextToken());
                    int b = Integer.parseInt(st.nextToken());
                    col(matrix, a, b);
                } else if ("inc".equals(op)) {
                    inc(matrix);
                } else if ("dec".equals(op)) {
                    dec(matrix);
                }
            }
            System.out.println("Case #" + (k + 1));
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                for (int l = 0; l < n; l++) {
                    sb.append(matrix[j][l]);
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
        }
    }

    private static void print(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < matrix.length; j++) {
            for (int l = 0; l < matrix.length; l++) {
                sb.append(matrix[j][l]);
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private static void dec(int[][] matrix) {
        for (int k = 0; k < matrix.length; k++) {
            for (int j = 0; j < matrix.length; j++) {
                matrix[k][j]--;
                if (matrix[k][j] == -1) {
                    matrix[k][j] = 9;
                }
            }
        }
    }

    private static void inc(int[][] matrix) {
        for (int k = 0; k < matrix.length; k++) {
            for (int j = 0; j < matrix.length; j++) {
                matrix[k][j]++;
                if (matrix[k][j] == 10) {
                    matrix[k][j] = 0;
                }
            }
        }
    }

    private static void col(int[][] matrix, int a, int b) {
        a--;
        b--;
        for (int k = 0; k < matrix.length; k++) {
            int aux = matrix[k][a];
            matrix[k][a] = matrix[k][b];
            matrix[k][b] = aux;
        }
    }

    private static void row(int[][] matrix, int a, int b) {
        a--;
        b--;
        for (int k = 0; k < matrix.length; k++) {
            int aux = matrix[a][k];
            matrix[a][k] = matrix[b][k];
            matrix[b][k] = aux;
        }
    }

    private static int[][] transpose(int[][] matrix) {
        int[][] transposed = new int[matrix.length][matrix[0].length];
        for (int k = 0; k < matrix.length; k++) {
            for (int j = 0; j < matrix.length; j++) {
                transposed[j][k] = matrix[k][j];
            }
        }
        return transposed;
    }

    static String readLn() {
        String newLine = System.getProperty("line.separator");
        StringBuilder buffer = new StringBuilder();
        int car = -1;
        try {
            car = System.in.read();
            while ((car > 0) && (car != newLine.charAt(0))) {
                buffer.append((char) car);
                car = System.in.read();
            }
            if (car == newLine.charAt(0)) System.in.skip(newLine.length() - 1);
        } catch (java.io.IOException e) {
            return (null);
        }
        if ((car < 0) && (buffer.length() == 0)) return (null);
        return (buffer.toString()).trim();
    }
}
