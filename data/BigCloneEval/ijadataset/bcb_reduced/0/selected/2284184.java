package test;

import java.util.Random;
import utils.exceptions.OptimizingException;
import utils.methods.convex.ArrowGurvic;
import utils.methods.convex.KunTakker;
import utils.methods.nonlinear.LagrangeMultiplier;
import utils.methods.nonlinear.MonteCarlo;
import utils.methods.nonlinear.Zoitendake;
import utils.methods.square.LevenbergMarquardt;
import utils.methods.square.WolfFrank;
import utils.portfolio.Portfolio;

public class TestMain {

    /**
	 * @param args
	 * @throws OptimizingException
	 */
    private static void test(Portfolio port, double exp, double e) throws OptimizingException {
        System.out.println("LagrangeMultiplier");
        LagrangeMultiplier method = new LagrangeMultiplier(port, exp, e);
        method.evaluate();
        System.out.println("Operations = " + method.getOperations());
        System.out.println("Time = " + method.getTime() + "\n");
        System.out.println("MonteCarlo");
        MonteCarlo method2 = new MonteCarlo(port, exp, e);
        method2.evaluate();
        System.out.println("Operations = " + method2.getOperations());
        System.out.println("Time = " + method2.getTime() + "\n");
        System.out.println("Zojtendake");
        Zoitendake method3 = new Zoitendake(port, exp, e);
        method3.evaluate();
        System.out.println("Operations = " + method3.getOperations());
        System.out.println("Time = " + method3.getTime() + "\n");
        System.out.println("KunTakker");
        KunTakker method4 = new KunTakker(port, exp, e);
        method4.evaluate();
        System.out.println("Operations = " + method4.getOperations());
        System.out.println("Time = " + method4.getTime() + "\n");
        System.out.println("ArrowHurvic");
        ArrowGurvic method5 = new ArrowGurvic(port, 0.001, exp, e);
        method5.evaluate();
        System.out.println("Operations = " + method5.getOperations());
        System.out.println("Time = " + method5.getTime() + "\n");
        System.out.println("WolfFrank");
        WolfFrank method6 = new WolfFrank(port, exp, e);
        method6.evaluate();
        System.out.println("Operations = " + method6.getOperations());
        System.out.println("Time = " + method6.getTime() + "\n");
        System.out.println("LevenbergMarquardt");
        LevenbergMarquardt method7 = new LevenbergMarquardt(port, exp, e);
        System.out.println("Operations = " + method7.getOperations());
        System.out.println("Time = " + method7.getTime() + "\n");
        System.out.println(method.getTime() + " " + method2.getTime() + " " + method3.getTime() + " " + method4.getTime() + " " + method5.getTime() + " " + method6.getTime() + " " + method7.getTime());
        System.out.println(method.getOperations() + " " + method2.getOperations() + " " + method3.getOperations() + " " + method4.getOperations() + " " + method5.getOperations() + " " + method6.getOperations() + " " + method7.getOperations());
    }

    public static void main(String[] args) {
        int size = 10;
        Portfolio port = new Portfolio(size);
        double[] data = new double[size];
        Random rnd = new Random();
        double max = 0, min = 0;
        for (int i = 0; i < size; i++) {
            data[i] = Math.abs(rnd.nextInt() * (1 - rnd.nextDouble()));
            if (max < data[i]) max = data[i];
            if (min > data[i]) min = data[i];
        }
        port.setProfit(data);
        double[][] data2 = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    data2[i][j] = (rnd.nextBoolean() ? (-1) : 1) * (1 - rnd.nextDouble());
                    data2[j][i] = data2[i][j];
                } else {
                    data2[i][i] = (1 - rnd.nextDouble());
                }
            }
        }
        port.setCovariance(data2);
        double exp = (max + min) / 2;
        try {
            test(port, exp, 0.00000001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
