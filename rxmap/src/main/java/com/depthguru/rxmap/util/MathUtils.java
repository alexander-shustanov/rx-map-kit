package com.depthguru.rxmap.util;

/**
 * MathUtils
 * </p>
 * alexander.shustanov on 22.12.16
 */
public class MathUtils {
    public static int mod(int number, int modulus) {
        if (number > 0)
            return number % modulus;

        while (number < 0)
            number += modulus;

        return number;
    }

    public static boolean around(int val, int left, int right) {
        return left <= val && val <= right;
    }
}
