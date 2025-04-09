package hr.fer.zemris.java.tecaj_1.vjezba;

public class Zadatak01_02 {

    public static void main(String[] args) {
        System.out.println(args[0] + ". Fibonaccijev broj je " + fibonacci(Integer.parseInt(args[0])) + ".");
    }

    public static int fibonacci(int broj) {
        if (broj <= 1) return broj; else return fibonacci(broj - 1) + fibonacci(broj - 2);
    }
}
