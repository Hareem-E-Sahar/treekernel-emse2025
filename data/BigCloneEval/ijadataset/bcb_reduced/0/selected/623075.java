package smo;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.rmi.*;
import java.math.*;
import java.text.DecimalFormat;
import modulosvm.SparseVector;

class sparse_binary_vector {

    Vector id = new Vector();
}

public class Smo {

    /**
     * Numero de amostras
     */
    public int N = 0;

    /**
     * Numero de parametros
     */
    public int d = -1;

    /**
     * Vetor de Multiplicadores de Lagrange
     */
    public Vector alph = new Vector();

    /**
     * Limiar b (bias)
     */
    public float b = 0;

    /**
     * Vetor de pesos ajustaveis w
     */
    public Vector w = new Vector();

    /**
     * Erros cache
     */
    public Vector error_cache = new Vector();

    /**
     * Vetor para dados esparcos e binarios
     */
    public Vector sparse_binary_points = new Vector();

    /**
     * Vetor para dados esparcos
     */
    public Vector sparse_points = new Vector();

    /**
     * Vetor de rotulos (classe/objetivo/alvo)
     */
    public Vector target = new Vector();

    /**
     * Tamanho da matriz de pontos
     */
    public int MATRIX = 1000;

    /**
     * Matriz de pontos
     */
    public float dense_points[][] = new float[MATRIX][MATRIX];

    public int first_test_i = 0;

    /**
     * support vectors are within [0..end_support_i)
     */
    public int end_support_i = -1;

    public Vector precomputed_self_dot_product = new Vector();

    public float precomputed_dot_product[][] = new float[MATRIX][MATRIX];

    public int learned_func_flag = -1;

    public int dot_product_flag = -1;

    public int kernel_flag = -1;

    public float delta_b = 0;

    public float C = (float) 0.05;

    public float tolerance = (float) 0.001;

    public float eps = (float) 0.001;

    public float two_sigma_squared = 2;

    public boolean is_sparse_data = true;

    public boolean is_binary = false;

    public boolean is_libsvm_file = true;

    public boolean is_test_only = false;

    public boolean is_linear_kernel = true;

    /**
     * Construtor
     */
    public Smo() {
        for (int i = 0; i < MATRIX; i++) for (int j = 0; j < MATRIX; j++) dense_points[i][j] = 0;
    }

    /**
     * Transforma um objeto em float
     * @param o - objeto a ser transformado
     * @return  - valor float
     */
    public float object2float(Object o) {
        Float result = (Float) o;
        return result.floatValue();
    }

    /**
     * Transforma um objeto em int
     * @param o - objeto a ser transformado
     * @return  - valor int
     */
    public int object2int(Object o) {
        Integer result = (Integer) o;
        return result.intValue();
    }

    /**
     *
     * @param i1
     * @return
     */
    public int examineExample(int i1) {
        float y1 = 0, alph1 = 0, E1 = 0, r1 = 0;
        y1 = object2int(target.elementAt(i1));
        alph1 = object2float(alph.elementAt(i1));
        if (alph1 > 0 && alph1 < C) E1 = object2float(error_cache.elementAt(i1)); else E1 = learned_func(i1, learned_func_flag) - y1;
        r1 = y1 * E1;
        if ((r1 < -tolerance && alph1 < C) || (r1 > tolerance && alph1 > 0)) {
            {
                int k = 0, i2 = 0;
                float tmax = 0;
                for (i2 = (-1), tmax = 0, k = 0; k < end_support_i; k++) if (object2float(alph.elementAt(k)) > 0 && object2float(alph.elementAt(k)) < C) {
                    float E2 = 0, temp = 0;
                    E2 = object2float(error_cache.elementAt(k));
                    temp = Math.abs(E1 - E2);
                    if (temp > tmax) {
                        tmax = temp;
                        i2 = k;
                    }
                }
                if (i2 >= 0) {
                    if (takeStep(i1, i2) == 1) {
                        return 1;
                    }
                }
            }
            float rands = 0;
            {
                int k = 0, k0 = 0;
                int i2 = 0;
                for (rands = (float) Math.random(), k0 = (int) (rands * end_support_i), k = k0; k < end_support_i + k0; k++) {
                    i2 = k % end_support_i;
                    if (object2float(alph.elementAt(i2)) > 0 && object2float(alph.elementAt(i2)) < C) {
                        if (takeStep(i1, i2) == 1) {
                            return 1;
                        }
                    }
                }
            }
            {
                int k0 = 0, k = 0, i2 = 0;
                rands = 0;
                for (rands = (float) Math.random(), k0 = (int) (rands * end_support_i), k = k0; k < end_support_i + k0; k++) {
                    i2 = k % end_support_i;
                    if (takeStep(i1, i2) == 1) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * y1 = d1
     * y2 = d2
     * alph1 = a1velho
     * alph2 = a2velho
     * a1 = a1novo
     * a2 = a2novo
     * H = U
     * L = V
     *
     */
    public int takeStep(int i1, int i2) {
        int y1 = 0, y2 = 0, s = 0;
        float alph1 = 0, alph2 = 0;
        float a1 = 0, a2 = 0;
        float E1 = 0, E2 = 0, L = 0, H = 0, k11 = 0, k22 = 0, k12 = 0, eta = 0, Lobj = 0, Hobj = 0;
        if (i1 == i2) return 0;
        {
            alph1 = object2float(alph.elementAt(i1));
            y1 = object2int(target.elementAt(i1));
            if (alph1 > 0 && alph1 < C) E1 = object2float(error_cache.elementAt(i1)); else E1 = learned_func(i1, learned_func_flag) - y1;
        }
        {
            alph2 = object2float(alph.elementAt(i2));
            y2 = object2int(target.elementAt(i2));
            if (alph2 > 0 && alph2 < C) E2 = object2float(error_cache.elementAt(i2)); else E2 = learned_func(i2, learned_func_flag) - y2;
        }
        s = y1 * y2;
        if (y1 == y2) {
            float gamma = alph1 + alph2;
            if (gamma > C) {
                L = gamma - C;
                H = C;
            } else {
                L = 0;
                H = gamma;
            }
        } else {
            float gamma = alph1 - alph2;
            if (gamma > 0) {
                L = 0;
                H = C - gamma;
            } else {
                L = -gamma;
                H = C;
            }
        }
        if (L == H) {
            return 0;
        }
        k11 = kernel_func(i1, i1, kernel_flag);
        k12 = kernel_func(i1, i2, kernel_flag);
        k22 = kernel_func(i2, i2, kernel_flag);
        eta = 2 * k12 - k11 - k22;
        if (eta < 0) {
            a2 = alph2 + y2 * (E2 - E1) / eta;
            if (a2 < L) a2 = L; else if (a2 > H) a2 = H;
        } else {
            {
                float c1 = eta / 2;
                float c2 = y2 * (E1 - E2) - eta * alph2;
                Lobj = c1 * L * L + c2 * L;
                Hobj = c1 * H * H + c2 * H;
            }
            if (Lobj > Hobj + eps) a2 = L; else if (Lobj < Hobj - eps) a2 = H; else a2 = alph2;
        }
        if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps)) return 0;
        a1 = alph1 - s * (a2 - alph2);
        if (a1 < 0) {
            a2 += s * a1;
            a1 = 0;
        } else if (a1 > C) {
            float t = a1 - C;
            a2 += s * t;
            a1 = C;
        }
        {
            float b1 = 0, b2 = 0, bnew = 0;
            if (a1 > 0 && a1 < C) bnew = b + E1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12; else {
                if (a2 > 0 && a2 < C) bnew = b + E2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22; else {
                    b1 = b + E1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12;
                    b2 = b + E2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22;
                    bnew = (b1 + b2) / 2;
                }
            }
            delta_b = bnew - b;
            b = bnew;
        }
        if (is_linear_kernel) {
            float t1 = y1 * (a1 - alph1);
            float t2 = y2 * (a2 - alph2);
            if (is_sparse_data && is_binary) {
                int p1 = 0, num1 = 0, p2 = 0, num2 = 0;
                num1 = ((sparse_binary_vector) sparse_binary_points.elementAt(i1)).id.size();
                for (p1 = 0; p1 < num1; p1++) {
                    int temp0 = object2int(((sparse_binary_vector) sparse_binary_points.elementAt(i1)).id.elementAt(p1));
                    float temp = object2float(w.elementAt(temp0));
                    w.set(temp0, new Float(temp + t1));
                }
                num2 = ((sparse_binary_vector) sparse_binary_points.elementAt(i2)).id.size();
                for (p2 = 0; p2 < num2; p2++) {
                    int temp0 = object2int(((sparse_binary_vector) sparse_binary_points.elementAt(i2)).id.elementAt(p2));
                    float temp = object2float(w.elementAt(temp0));
                    w.set(temp0, new Float(temp + t2));
                }
            } else if (is_sparse_data && !is_binary) {
                int p1 = 0, num1 = 0, p2 = 0, num2 = 0;
                num1 = ((SparseVector) sparse_points.elementAt(i1)).id.size();
                for (p1 = 0; p1 < num1; p1++) {
                    int temp1 = object2int(((SparseVector) sparse_points.elementAt(i1)).id.elementAt(p1));
                    float temp = object2float(w.elementAt(temp1));
                    float temp2 = object2float(((SparseVector) sparse_points.elementAt(i1)).val.elementAt(p1));
                    w.set(temp1, new Float(temp + t1 * temp2));
                }
                num2 = ((SparseVector) sparse_points.elementAt(i2)).id.size();
                for (p2 = 0; p2 < num2; p2++) {
                    int temp1 = object2int(((SparseVector) sparse_points.elementAt(i2)).id.elementAt(p2));
                    float temp = object2float(w.elementAt(temp1));
                    float temp2 = object2float(((SparseVector) sparse_points.elementAt(i2)).val.elementAt(p2));
                    temp = temp + t2 * temp2;
                    Float value = new Float(temp);
                    w.set(temp1, value);
                }
            } else for (int i = 0; i < d; i++) {
                float temp = dense_points[i1][i] * t1 + dense_points[i2][i] * t2;
                float temp1 = object2float(w.elementAt(i));
                Float value = new Float(temp + temp1);
                w.set(i, value);
            }
        }
        {
            float t1 = y1 * (a1 - alph1);
            float t2 = y2 * (a2 - alph2);
            for (int i = 0; i < end_support_i; i++) if (0 < object2float(alph.elementAt(i)) && object2float(alph.elementAt(i)) < C) {
                float tmp = object2float(error_cache.elementAt(i));
                tmp += t1 * kernel_func(i1, i, kernel_flag) + t2 * kernel_func(i2, i, kernel_flag) - delta_b;
                error_cache.set(i, new Float(tmp));
            }
            error_cache.set(i1, new Float(0));
            error_cache.set(i2, new Float(0));
        }
        alph.set(i1, new Float(a1));
        alph.set(i2, new Float(a2));
        return 1;
    }

    /**
     *
     * @param k
     * @return
     */
    float learned_func_linear_sparse_binary(int k) {
        float s = 0;
        int temp = 0;
        for (int i = 0; i < ((sparse_binary_vector) sparse_binary_points.elementAt(k)).id.size(); i++) {
            temp = object2int(((sparse_binary_vector) sparse_binary_points.elementAt(i)).id.elementAt(i));
            s += object2float(w.elementAt(temp));
        }
        s -= b;
        return s;
    }

    /**
     *
     * @param k
     * @return
     */
    public float learned_func_linear_sparse_nonbinary(int k) {
        float s = 0;
        for (int i = 0; i < ((SparseVector) sparse_points.elementAt(k)).id.size(); i++) {
            int j = object2int(((SparseVector) sparse_points.elementAt(k)).id.elementAt(i));
            float v = object2float(((SparseVector) sparse_points.elementAt(k)).val.elementAt(i));
            s += object2float(w.elementAt(j)) * v;
        }
        s -= b;
        return s;
    }

    /**
     *
     * @param k
     * @return
     */
    public float learned_func_linear_dense(int k) {
        float s = 0;
        for (int i = 0; i < d; i++) s += object2float(w.elementAt(i)) * dense_points[k][i];
        s -= b;
        return s;
    }

    /**
     * Determina a distancia do ponto yi até o atual hiperplano
     * @param k
     * @return
     */
    public float learned_func_nonlinear(int k) {
        float s = 0;
        for (int i = 0; i < end_support_i; i++) if (object2float(alph.elementAt(i)) > 0) {
            s += object2float(alph.elementAt(i)) * object2int(target.elementAt(i)) * kernel_func(i, k, kernel_flag);
        }
        s -= b;
        return s;
    }

    /**
     * produto escalar esparço e binario
     * no caso sparço cada entrada pode ter o numero de atributos variavel
     * por ser binario, retira os valores do sparce_binary_points
     * @param i1
     * @param i2
     * @return
     */
    public float dot_product_sparse_binary(int i1, int i2) {
        int p1 = 0, p2 = 0, dot = 0;
        int num1 = ((sparse_binary_vector) sparse_binary_points.elementAt(i1)).id.size();
        int num2 = ((sparse_binary_vector) sparse_binary_points.elementAt(i2)).id.size();
        while (p1 < num1 && p2 < num2) {
            int a1 = object2int(((sparse_binary_vector) sparse_binary_points.elementAt(i1)).id.elementAt(p1));
            int a2 = object2int(((sparse_binary_vector) sparse_binary_points.elementAt(i2)).id.elementAt(p2));
            if (a1 == a2) {
                dot++;
                p1++;
                p2++;
            } else if (a1 > a2) p2++; else p1++;
        }
        return (float) dot;
    }

    /**
     * Produto escalar esparco e não binario
     * no caso esparço cada entrada pode ter o numero de atributos variavel
     * por nao ser binario, retira os valores do sparce_points
     * @param i1
     * @param i2
     * @return
     */
    public float dot_product_sparse_nonbinary(int i1, int i2) {
        int p1 = 0, p2 = 0;
        float dot = 0;
        int num1 = ((SparseVector) sparse_points.elementAt(i1)).id.size();
        int num2 = ((SparseVector) sparse_points.elementAt(i2)).id.size();
        while (p1 < num1 && p2 < num2) {
            int a1 = object2int(((SparseVector) sparse_points.elementAt(i1)).id.elementAt(p1));
            int a2 = object2int(((SparseVector) sparse_points.elementAt(i2)).id.elementAt(p2));
            if (a1 == a2) {
                float val1 = object2float(((SparseVector) sparse_points.elementAt(i1)).val.elementAt(p1));
                float val2 = object2float(((SparseVector) sparse_points.elementAt(i2)).val.elementAt(p2));
                dot += val1 * val2;
                p1++;
                p2++;
            } else if (a1 > a2) p2++; else p1++;
        }
        return (float) dot;
    }

    /**
     * Produto Escalar Denso
     * @param i1
     * @param i2
     * @return
     */
    public float dot_product_dense(int i1, int i2) {
        float dot = 0;
        for (int i = 0; i < d; i++) dot += dense_points[i1][i] * dense_points[i2][i];
        return dot;
    }

    /**
     * Kernel rbf (gaussiano)
     * @param i1
     * @param i2
     * @return
     */
    public float rbf_kernel(int i1, int i2) {
        float s = this.precomputed_dot_product[i1][i2];
        s *= -2;
        s += object2float(precomputed_self_dot_product.elementAt(i1)) + object2float(precomputed_self_dot_product.elementAt(i2));
        return (float) Math.exp((float) (-s / two_sigma_squared));
    }

    /**
     * Error
     * @return
     */
    public float error_rate() {
        int n_total = 0;
        int n_error = 0;
        for (int i = first_test_i; i < N; i++) {
            if ((learned_func(i, learned_func_flag) > 0) != (object2int(target.elementAt(i)) > 0)) {
                n_error++;
            }
            n_total++;
        }
        System.out.println(n_error + "/" + n_total);
        return (float) n_error / (float) n_total;
    }

    /**
     * Error
     * @return
     */
    public HashMap error_rate2() {
        HashMap retorno = new HashMap();
        float learn = (learned_func(0, learned_func_flag));
        if (learn > 0) {
            retorno.put("learn", learn);
            retorno.put("saida", 1);
            return retorno;
        } else {
            retorno.put("learn", learn);
            retorno.put("saida", -1);
            return retorno;
        }
    }

    /**
     * Redireciona o Produto Escalar
     * @param i
     * @param j
     * @param flag
     * @return result
     */
    public float dot_product_func(int i, int j, int flag) {
        float result = 0;
        if (flag == 1) result = dot_product_sparse_binary(i, j); else if (flag == 2) result = dot_product_sparse_nonbinary(i, j); else if (flag == 3) result = dot_product_dense(i, j);
        return result;
    }

    /**
     * Redireciona o tipo de Aprendizado
     * @param i
     * @param flag
     * @return  result
     */
    public float learned_func(int i, int flag) {
        float result = 0;
        if (flag == 1) result = learned_func_linear_sparse_binary(i); else if (flag == 2) result = learned_func_linear_sparse_nonbinary(i); else if (flag == 3) result = learned_func_linear_dense(i); else if (flag == 4) result = learned_func_nonlinear(i);
        return result;
    }

    /**
     * Redireciona para o Kernel
     * @param i
     * @param j
     * @param flag
     * @return
     */
    public float kernel_func(int i, int j, int flag) {
        float result = 0;
        if (flag == 1) result = dot_product_func(i, j, this.dot_product_flag); else if (flag == 2) result = rbf_kernel(i, j);
        return result;
    }

    /**
     * Muda o tamanho do vetor passado por parametro
     * @param v         - vetor a ser mudado o tamanho
     * @param newSize   - novo tamanho
     * @param type      - tipo do dado - 1:integer, 2:float
     */
    public void resize(Vector v, int newSize, int type) {
        int original = v.size();
        if (original > newSize) {
            v.setSize(newSize);
            return;
        }
        for (int i = original; i < newSize; i++) {
            if (type == 1) v.add(new Integer(0)); else if (type == 2) v.add(new Float(0));
        }
    }

    /**
     * Reserva espaço no vetor
     * @param v     - vetor a ser reservado o espaço
     * @param size  - tamanho a ser reservado
     * @param type  - tipo de dado - 1: integer, 2: float
     */
    public void reserve(Vector v, int size, int type) {
        for (int i = 0; i < size; i++) {
            if (type == 1) v.add(i, new Integer(0)); else if (type == 2) v.add(i, new Float(0));
        }
    }

    /**
     * Reserva espaço no SparceVector
     * @param v     - Vetor a ser reservado espaço
     * @param size  - tamanho a ser reservado
     */
    public void reserveSparse(Vector v, int size) {
        for (int i = 0; i < size; i++) {
            v.add(i, new SparseVector());
        }
    }

    /**
     * Reserva espaço em vetor binario
     * @param v     - Vetor a ser reservado espaço
     * @param size  - tamanho a ser reservado
     */
    public void reserveSparseBinary(Vector v, int size) {
        for (int i = 0; i < size; i++) v.add(i, new sparse_binary_vector());
    }

    /**
     * Reserva espaço em uma matriz
     * @param array - matriz a ser reservado o espaço
     * @param size  - tamanho a ser reservado
     */
    public void reserve(float[][] array, int size) {
        for (int i = 0; i < size; i++) for (int j = 0; j < d; j++) array[i][j] = 0;
    }
}
