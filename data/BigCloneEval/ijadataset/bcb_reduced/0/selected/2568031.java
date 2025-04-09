package edu.java.texbooks.scjp.test04;

import java.io.IOException;

public class ReverseString {

    public ReverseString() throws IOException {
        System.out.println("Here we might have IOException");
    }

    @Override
    public String toString() {
        return "Hello, people";
    }

    public void print() {
        System.out.println(this.toString());
    }

    public static void main(String[] args) {
        myMethod();
    }

    public static int fib(int n) throws Exception {
        if (n <= 1) {
            return 1;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    public static void myMethod() {
        System.out.println("Do something in myMethod() ");
        try {
            throw new PerfectException();
        } catch (PerfectException pe) {
        }
    }

    static String reverse(String string) throws Exception {
        if (string == null) {
            throw new NullPointerException("The String object is null-pointed.");
        }
        if (string.length() == 0) {
            throw new Exception("The string has no symbols.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = string.length() - 1; 0 <= i; i--) {
            sb.append(string.charAt(i));
        }
        return sb.toString();
    }
}
