package com.test;

/**
 * @author LvSaiHui {gurnfe@163.com}
 * @version 1.0 <br>
 *          Copyright (C), 2007-2008, ZJUT <br>
 *          This program is protected by copyright laws. <br>
 *          Program Name: Initial.java <br>
 *          Date: 2009-3-12 <br>
 *          Description:
 */
public class Initial {

    int a = 9;

    {
        int a = 5;
    }

    public static int gongYue(int a, int b) {
        int m = 1;
        if (a < b) {
            m = a;
            a = b;
            b = m;
        }
        while (m != 0) {
            m = a % b;
            a = b;
            b = m;
        }
        return a;
    }

    public static void main(String[] args) {
        System.out.print(gongYue(6, 2));
    }
}
