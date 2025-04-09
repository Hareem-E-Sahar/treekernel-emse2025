package methods;

import exceptions.RaizNoEncontradaExcepcion;

/**
 * <p>El metodo de biseccion es un algoritmo de busqueda de raices que trabaja
 * dividiendo el intervalo a la mitad y seleccionando el subintervalo que tiene
 * la raiz. Supongase que queremos resolver la ecuaci�n f(x) = 0 (donde f es
 * continua). Dados dos puntos a y b tal que f(a) y f(b) tengan signos
 * distintos, sabemos por el Teorema de Bolzano que f debe tener, al menos, una
 * raiz en el intervalo [a, b]. El metodo de biseccion divide el intervalo en
 * dos, usando un tercer punto c = (a+b) / 2. En este momento, existen dos
 * posibilidades: f(a) y f(c), o f(c) y f(b) tienen distinto signo. El algoritmo
 * de biseccion se aplica al subintervalo donde el cambio de signo ocurre.</p>
 * 
 * <img alt="Bisection"  height="250"  width="291"  
 * src="..\resources\Bisection_method.png" >
 *
 * <br>
 * <b> Condiciones suficientes </b>: que haya una raiz en el intervalo, que esa
 * raiz sea �nica (f'(x) != 0 para todo x perteneciente a [left, right]), la
 * funcion debe ser continua en el intervalo y f(left)debe tener distinto signo
 * que f(right)
 */
public class Biseccion {

    private static final String METHOD_NAME = "Bisection";

    /**
     * El metodo de Biseccion encuentra la raiz existente entre 2 valores conocidos.
     *
     * @param a          el valor de la izquierda
     * @param b          el valor de la derecha
     * @param error      el nivel de error tolerado
     * @param iterations el maximomo numero de iteraciones permitidas
     * @return raiz de la funcion
     * @throws RaizNoEncontradaExcepcion en caso de que la raiz no haya podidod ser encontrada
     * o que la funcion no cumpla con las condiciones minimas necesarias
     */
    public static double findRoot(Funcion f, double a, double b, double error, int iterations) throws RaizNoEncontradaExcepcion {
        int i = 1;
        double fa = f.eval(a);
        while (i <= iterations) {
            double p = a + (b - a) / 2;
            double fp = f.eval(p);
            if (fp == 0 || (b - a) / 2 < error) {
                return p;
            }
            i += 1;
            if (fa * fp > 0) {
                a = p;
                fa = fp;
            } else {
                b = p;
            }
        }
        throw new RaizNoEncontradaExcepcion(METHOD_NAME);
    }
}
