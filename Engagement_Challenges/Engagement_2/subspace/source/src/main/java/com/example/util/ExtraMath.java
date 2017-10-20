package com.example.util;

/**
 * Extra math functions missing from java's standard library.
 */
public class ExtraMath
{
    /**
     * Compute the canonical form of x mod base.
     *
     * This is similar to the '%' operator, except that the return
     * value will always be non-negative for non-negative bases.
     */
    public static double modulus(
        double base,
        double x)
    {
        double result = x % base;
        if (result < 0.0)
        {
            result = (result + base) % base;
        }

        assert
            result >= 0.0 && result < base
            : x + " mod " + base + " = " + result;

        return result;
    }

    /**
     * @see #modulus(double, double)
     */
    public static int modulus(
        int base,
        int x)
    {
        int result = (x % base + base) % base;

        assert
            result >= 0 && result < base
            : x + " mod " + base + " = " + result;

        return result;
    }

    /**
     * Use modular arithmetic to normalize {@code x} into the range
     * {@code [-base/2, base/2)}.
     *
     * This is equal to {@code modulus(base, x + base/2.0) -
     * base/2.0}, but it avoids some rounding errors.
     */
    public static double centeredModulus(
        double base,
        double x)
    {
        double halfBase = base / 2.0;

        double result;

        if (x >= -halfBase && x < halfBase)
        {
            result = x;
        }
        else if (x < 0.0)
        {
            result = modulus(base, x + halfBase) - halfBase;
        }
        else
        {
            result = modulus(base, x - halfBase) - halfBase;
        }

        assert
            result >= -halfBase && result < halfBase
            : x + " centerMod " + base + " = " + result;

        return result;
    }

    /**
     * Return the next up number after {@code x} in the group mod
     * {@code base}.
     */
    public static double nextUp(
        double base,
        double x)
    {
        double result = modulus(base, Math.nextUp(x));

        assert
            true
                && result >= 0.0
                && result < base
                && (result > x || result == 0.0)
            : "nextUp(" + base + ", " + x + ") = " + result;

        return result;
    }

    /**
     * Return the next down number after {@code x} in the group mod
     * {@code base}.
     */
    public static double nextDown(
        double base,
        double x)
    {
        double result;

        if (x == 0.0)
        {
            result = Math.nextAfter(base, Double.NEGATIVE_INFINITY);
        }
        else
        {
            result = Math.nextAfter(x, Double.NEGATIVE_INFINITY);
        }

        assert
            true
                && result >= 0.0
                && result < base
                && (result < x || x == 0.0)
            : "nextDown(" + base + ", " + x + ") = " + result;

        return result;
    }

    /**
     * Compute the average of x and y.
     *
     * The requirements and implementation of this method are based
     * off of http://www.ibiblio.org/pub/languages/fortran/ch4-9.html.
     */
    public static double average(
        double x,
        double y)
    {
        double result;

        boolean sameSign;
        if (x >= 0.0)
        {
            if (y >= 0.0)
            {
                sameSign = true;
            }
            else
            {
                sameSign = false;
            }
        }
        else
        {
            if (y >= 0.0)
            {
                sameSign = false;
            }
            else
            {
                sameSign = true;
            }
        }

        if (sameSign)
        {
            if (y >= x)
            {
                result = x + ((y - x) / 2.0);
            }
            else
            {
                result = y + ((x - y) / 2.0);
            }
        }
        else
        {
            result = (x + y) / 2.0;
        }

        assert
            true
                && result >= Math.min(x, y)
                && result <= Math.max(x, y)
                && (false
                    || Math.nextUp(Math.min(x, y)) >= Math.max(x, y)
                    || (result != x && result != y)
                    )
            : "average(" + x + ", " + y + ") = " + result;

        return result;
    }

    /**
     * Compute the midpoint from {@code x} to {@code y} in the group
     * of doubles mod {@code base}.
     *
     * This is similar to an average, but it's directed as if {@code x
     * <= y}. For all valid values, {@code midpoint(base, x, y)} is
     * mathematically equal to {@code modulus(base, midpoint(base, y,
     * x) + base/2.0)}, though rounding errors may make that untrue in
     * Java.
     */
    public static double midpoint(
        double base,
        double x,
        double y)
    {
        double result;

        x = modulus(base, x);
        y = modulus(base, y);

        if (x <= y)
        {
            result = x + ((y - x) / 2.0);
        }
        else if (x + y >= base)
        {
            result = (y + (x - base)) / 2.0;
        }
        else if (Math.nextUp(x) == base && y < Math.ulp(x))
        {
            result = 0.0;
        }
        else
        {
            result = y + ((x - y) / 2.0) + base / 2.0;
        }

        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled)
        {
            String message =
                "midpoint(" + base + ", " + x + ", " + y + ")" +
                " = " + result;

            if (x <= y)
            {
                assert result >= x && result <= y : message;
            }
            else
            {
                assert result >= x || result <= y : message;
            }

            double xp = ExtraMath.nextUp(base, x);
            double xpp = ExtraMath.nextUp(base, xp);
            if (x != y && xp != y && xpp != y)
            {
                assert result != x && result != y : message;
            }
        }

        return result;
    }

    /**
     * Compute the geometric mean of x and y.
     */
    public static double geometricMean(
        double x,
        double y)
    {
        assert x >= 0.0;
        assert y >= 0.0;

        double result;

        double xy = x * y;
        if (Double.isInfinite(xy))
        {
            result = Math.sqrt(x) * Math.sqrt(y);
        }
        else
        {
            result = Math.sqrt(xy);
        }

        assert
            true
                && result >= Math.min(x, y)
                && result <= Math.max(x, y)
                && (false
                    || Math.nextUp(Math.min(x, y)) >= Math.max(x, y)
                    || x == 0.0
                    || y == 0.0
                    || (result != x && result != y)
                    )
            : "geometricMean(" + x + ", " + y + ") = " + result;

        return result;
    }
}
