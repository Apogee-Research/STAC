package com.stac.mathematics;

import com.stac.image.utilities.ARGB;

/**
 *
 */
public class Mathematics {
    private static final double Z255_inv = 1.0 / 255.0;
    private static final double Z255_e_revert = 255.0 / 2.71828182847;


    /**
     * Accuracy is a precalculated function which is used to determine how many
     * iterations are required to process an input argb tuple.
     */
    private static final int[] accuracy = new int[257];


    /** Accuracy is initialized here:
     * {@code
     * <pre>
     *                   (              1              )
     *                   (  -------------------------  )
     * accuracy[n] = 4 + (      ( |30 - n|          )  )
     *                   (  tan (---------- + 0.001 )  )
     *                   (      (   255             )  )
     * </pre>
     * }
     *
     * The tangent is the primary function providing the required explosive parameter.
     * Tangents cause exceptions when the parameter is 0, so to avoid this 0.001 is added
     * to the value passed to tan. This serves two purposes, the other being control of
     * the accuracy's maximum value.
     *
     * The |30 - n| term controls which pixel color will cause the bad case and the 255
     * term is simply there to bring values down to the range (-1, 1).
     */
    static {
        for (int n = 0; n <= 256; n++) {
            double v = 1.0 / Math.tan(Math.abs(30 - n) * Z255_inv + 0.001);
            accuracy[n] = 4 + (int) v;
        }
    }

    /**
     * Lanczos approximation of the log gamma function.
     */
    private static double lgamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0
                + 76.18009173000 / x
                - 86.50532033000 / (x + 1)
                + 24.01409282200 / (x + 2)
                - 01.23173951600 / (x + 3)
                + 00.00120858003 / (x + 4)
                - 00.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    /**
     * Lanczos approximation of the factorial function.
     */
    private static double factorial(double x) {
        return Math.exp(lgamma(x + 1));
    }

    private static double exp(int x, int n) {
        double exp_n = 0;
        for (int i = 0; i < n; i++) {
            double aDouble = Math.pow(x * Z255_inv, n);
            double aDouble1 = 1.0 / factorial(n);
            exp_n += aDouble * aDouble1;
        }
        return exp_n;
    }

    /**
     * intensify
     * Is a dummy term used to refer to this broken algorithm.
     * <p/>
     * The intensify parameters are not taken from the same pixel, but multiple
     * in the neighborhood. These neighborhood pixel are averaged using special
     * weight functions and finally passed to this method.
     *
     * @param a Alpha
     * @param r Red
     * @param g Green
     * @param b Blue
     * @return Packaged output.
     */
    public static int intensify(int a, int r, int g, int b) {
        int acc = accuracy[((r + g + b) % 255)];
        return ARGB.toARGB(
                a,
                (int) (Z255_e_revert * exp(r, acc)),
                (int) (Z255_e_revert * exp(g, acc)),
                (int) (Z255_e_revert * exp(b, acc))
        );
    }
}
