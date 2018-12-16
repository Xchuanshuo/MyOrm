package com.legend.orm.test;

/**
 * @author Legend
 * @data by on 18-12-14.
 * @description
 */
public class Test {

    public static long trailingZeros(long n) {
        int cal = 2;
        for (int i=3;i<=n;i++) {
            cal *= i;
        }
        long res = 0;
        while (cal%10 == 0) {
            res++;
            cal /= 10;
        }
        return cal;
    }

    public static void main(String[] args) {
//        trailingZeros(11);
        String str  = String.format("%s", "hello").toString();
        System.out.println("%"+str+"%");
    }
}
