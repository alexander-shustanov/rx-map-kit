package com.depthguru.rxmap.util;

import android.graphics.Rect;

import com.depthguru.rxmap.MapConstants;

public class GeometryMath {

    public static Rect getBoundingBoxForRotatedRect(Rect rect, int pivotX, int pivotY, double degrees, Rect reuse) {
        if (reuse == null) {
            reuse = new Rect();
        }

        double theta = degrees * MapConstants.DEG2RAD;
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double dx;
        double dy;

        dx = rect.left - pivotX;
        dy = rect.top - pivotY;
        double x1 = getRotatedX(pivotX, cosTheta, sinTheta, dx, dy);
        double y1 = getRotatedY(pivotY, cosTheta, sinTheta, dx, dy);

        dx = rect.right - pivotX;
        dy = rect.top - pivotY;
        double x2 = getRotatedX(pivotX, cosTheta, sinTheta, dx, dy);
        double y2 = getRotatedY(pivotY, cosTheta, sinTheta, dx, dy);

        dx = rect.right - pivotX;
        dy = rect.bottom - pivotY;
        double x3 = getRotatedX(pivotX, cosTheta, sinTheta, dx, dy);
        double y3 = getRotatedY(pivotY, cosTheta, sinTheta, dx, dy);

        dx = rect.left - pivotX;
        dy = rect.bottom - pivotY;
        double x4 = getRotatedX(pivotX, cosTheta, sinTheta, dx, dy);
        double y4 = getRotatedY(pivotY, cosTheta, sinTheta, dx, dy);

        reuse.set((int) Min4(x1, x2, x3, x4), (int) Min4(y1, y2, y3, y4),
                (int) Max4(x1, x2, x3, x4), (int) Max4(y1, y2, y3, y4));
        return reuse;
    }

    private static double getRotatedX(int pivotX, double cosTheta, double sinTheta, double dx, double dy) {
        return pivotX + dx * cosTheta - dy * sinTheta;
    }

    private static double getRotatedY(int pivotY, double cosTheta, double sinTheta, double dx, double dy) {
        return pivotY + dx * sinTheta + dy * cosTheta;
    }

    private static double Min4(double a, double b, double c, double d) {
        return Math.floor(Math.min(Math.min(a, b), Math.min(c, d)));
    }

    private static double Max4(double a, double b, double c, double d) {
        return Math.ceil(Math.max(Math.max(a, b), Math.max(c, d)));
    }
}

