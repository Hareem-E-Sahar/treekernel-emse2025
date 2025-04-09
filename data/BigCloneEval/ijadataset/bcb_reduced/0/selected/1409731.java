package com.dukesoftware.utils.math.gcd;

public class GCDReg implements IGCD {

    @Override
    public long gcd(long x, long y) {
        if (y == 0) return x; else return gcd(y, x % y);
    }

    @Override
    public long ngcd(int n, long[] a) {
        long d = a[0];
        for (int i = 1; i < n && d != 1; i++) d = gcd(a[i], d);
        return d;
    }
}
