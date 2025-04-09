import java.io.*;

class Matrix {

    double[][] data;

    private int widthV = 0;

    private int heightV = 0;

    private String name = "";

    public static void main(String[] args) {
        Matrix t = new Matrix(2, 2);
        t.data[0][0] = 1;
        t.data[1][0] = 0;
        t.data[0][1] = 2;
        t.data[1][1] = 1;
        t.print();
        t.inverse().print();
    }

    public Matrix(int m, int n) {
        heightV = m;
        widthV = n;
        data = new double[heightV][widthV];
        for (int i = 0; i < heightV; i++) for (int j = 0; j < widthV; j++) data[i][j] = 0.0;
    }

    public Matrix(Matrix m) {
        heightV = m.height();
        widthV = m.width();
        data = new double[heightV][widthV];
        for (int i = 0; i < heightV; i++) for (int j = 0; j < widthV; j++) data[i][j] = m.data[i][j];
    }

    public Matrix(double[][] graph) {
        heightV = graph[0].length;
        widthV = graph.length;
        data = new double[heightV][widthV];
        for (int i = 0; i < heightV; i++) for (int j = 0; j < widthV; j++) data[i][j] = graph[j][i];
    }

    void setName(String n) {
        this.name = n;
    }

    String getName() {
        return this.name;
    }

    int height() {
        return heightV;
    }

    int width() {
        return widthV;
    }

    public Matrix add(Matrix m) {
        if ((heightV == m.height()) && (widthV == m.width())) {
            Matrix a = new Matrix(this);
            for (int i = 0; i < heightV; i++) for (int j = 0; j < widthV; j++) a.data[i][j] += m.data[i][j];
            return a;
        } else {
            System.err.println("Matrix dimensions must match.");
            return null;
        }
    }

    public Matrix times(double n) {
        Matrix a = new Matrix(this);
        for (int i = 0; i < heightV; i++) for (int j = 0; j < widthV; j++) a.data[i][j] *= n;
        return a;
    }

    public Matrix times(Matrix m) {
        if (widthV == m.height()) {
            Matrix a = new Matrix(heightV, m.width());
            for (int i = 0; i < a.height(); i++) {
                for (int j = 0; j < a.width(); j++) {
                    double v = 0.0;
                    for (int r = 0; r < widthV; r++) {
                        v += this.data[i][r] * m.data[r][j];
                    }
                    a.data[i][j] = v;
                }
            }
            return a;
        } else {
            System.err.println("Matrix dimensions A.width() and B.height() must match.");
            return null;
        }
    }

    public Matrix transpose() {
        Matrix a = new Matrix(widthV, heightV);
        for (int i = 0; i < widthV; i++) {
            for (int j = 0; j < heightV; j++) {
                a.data[i][j] = this.data[j][i];
            }
        }
        return a;
    }

    public static Matrix identity(int size) {
        Matrix a = new Matrix(size, size);
        for (int i = 0; i < size; i++) {
            a.data[i][i] = 1.0;
        }
        return a;
    }

    public double determinant() {
        double result = 0.0;
        if (data.length == 1) {
            result = data[0][0];
            return result;
        }
        if (data.length == 2) {
            result = data[0][0] * data[1][1] - data[0][1] * data[1][0];
            return result;
        }
        for (int i = 0; i < data[0].length; i++) {
            Matrix temp = new Matrix(data.length - 1, data[0].length - 1);
            for (int j = 1; j < data.length; j++) {
                for (int k = 0; k < data[0].length; k++) {
                    if (k < i) {
                        temp.data[j - 1][k] = data[j][k];
                    } else if (k > i) {
                        temp.data[j - 1][k - 1] = data[j][k];
                    }
                }
            }
            result += data[0][i] * Math.pow(-1, (double) i) * temp.determinant();
        }
        return result;
    }

    private double minor(int r, int c) {
        Matrix a = new Matrix(heightV - 1, widthV - 1);
        int rCount = 0;
        int cCount = 0;
        for (int i = 0; i < heightV; i++) {
            cCount = 0;
            if (i == r) continue;
            for (int j = 0; j < widthV; j++) {
                if (j == c) continue;
                a.data[rCount][cCount] = data[i][j];
                cCount++;
            }
            rCount++;
        }
        return a.determinant();
    }

    private double cofactor(int r, int c) {
        return Math.pow(-1, r + c) * minor(r, c);
    }

    private Matrix cofactorMatrix() {
        Matrix a = new Matrix(widthV, widthV);
        for (int i = 0; i < widthV; i++) {
            for (int j = 0; j < widthV; j++) {
                a.data[i][j] = cofactor(j, i);
            }
        }
        return a;
    }

    public Matrix inverse() {
        if (widthV == heightV) {
            return this.cofactorMatrix().times(1.0 / this.determinant());
        } else {
            System.err.println("Matrix must be square.");
            return null;
        }
    }

    public void print() {
        for (int i = 0; i < heightV; i++) {
            System.out.print("[ ");
            for (int j = 0; j < widthV; j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println("]");
        }
    }

    public void printMatLab() {
        int i;
        int j;
        System.out.print("[ ");
        for (i = 0; i < heightV; i++) {
            for (j = 0; j < (widthV - 1); j++) {
                System.out.print(data[i][j] + ",");
            }
            if (i < (heightV - 1)) System.out.print(data[i][j] + ";"); else System.out.print(data[i][j]);
        }
        System.out.println("]");
    }

    public void matLabToFile(String filename) {
        matLabToFile(filename, false);
    }

    public void matLabToFile(String filename, boolean append) {
        FileOutputStream out;
        PrintStream p;
        int i;
        int j;
        try {
            out = new FileOutputStream(filename, append);
            p = new PrintStream(out, true);
            p.print(this.name + " = [");
            for (i = 0; i < heightV; i++) {
                for (j = 0; j < (widthV - 1); j++) {
                    p.print(data[i][j] + ",");
                }
                if (i < (heightV - 1)) p.println(data[i][j] + ";"); else p.print(data[i][j]);
            }
            p.println("];");
            p.close();
        } catch (Exception e) {
            System.err.println("Error writing array to specified file.");
        }
    }
}
