package com.onehao.io;

public class Test2 {

    public int compute(int n) {
        if (1 == n || 2 == n) {
            return 1;
        } else {
            return compute(n - 1) + compute(n - 2);
        }
    }

    public static void main(String[] args) {
        Test2 test = new Test2();
        System.out.println(test.compute(8));
    }
}
