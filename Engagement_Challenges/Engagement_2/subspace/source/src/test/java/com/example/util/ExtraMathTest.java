package com.example.util;

import org.junit.Assert;
import org.junit.Test;

public class ExtraMathTest
{
    /**
     * Assert that {@code actual} is within {@code delta} of {@code
     * expected} in the group mod {@code base}, or that {@code actual}
     * and {@code expected} are infinite and equal.
     */
    public static void assertEquals(
        String message,
        double base,
        double expected,
        double actual,
        double delta)
    {
        if (
            false
                || Double.isInfinite(expected)
                || Double.isInfinite(actual)
            )
        {
            if (message == null)
            {
                Assert.assertEquals(expected, actual, 0.0);
            }
            else
            {
                Assert.assertEquals(message, expected, actual, 0.0);
            }
            return;
        }

        if (
            Math.abs(
                ExtraMath.centeredModulus(base, actual - expected))
            > delta)
        {
            if (message == null)
            {
                message = "";
            }
            else
            {
                message = message + " ";
            }

            throw new AssertionError(
                message
                + "expected:<" + expected + ">"
                + " but was:<" + actual + ">");
        }
    }

    /**
     * @see #assertEquals(String, double, double, double, double)
     */
    public static void assertEquals(
        double base,
        double expected,
        double actual,
        double delta)
    {
        assertEquals(null, base, expected, actual, delta);
    }

    @Test
    public void testAverage()
    {
        final double[] NUMBERS = {-7.345, -1.0, 0.0, 1.0, 2.0};

        for (double x : NUMBERS)
        {
            Assert.assertEquals(x, ExtraMath.average(x, x), 0.0);

            double xp = Math.nextAfter(x, Double.POSITIVE_INFINITY);
            double xpp = Math.nextAfter(xp, Double.POSITIVE_INFINITY);
            double xm = Math.nextAfter(x, Double.NEGATIVE_INFINITY);
            double xmm = Math.nextAfter(xm, Double.NEGATIVE_INFINITY);

            Assert.assertEquals(x, ExtraMath.average(xm, xp), 0.0);
            Assert.assertEquals(x, ExtraMath.average(xp, xm), 0.0);
            Assert.assertEquals(x, ExtraMath.average(xmm, xpp), 0.0);
            Assert.assertEquals(x, ExtraMath.average(xpp, xmm), 0.0);
            Assert.assertEquals(xp, ExtraMath.average(x, xpp), 0.0);
            Assert.assertEquals(xp, ExtraMath.average(xpp, x), 0.0);
            Assert.assertEquals(xm, ExtraMath.average(xmm, x), 0.0);
            Assert.assertEquals(xm, ExtraMath.average(x, xmm), 0.0);

            for (double y : NUMBERS)
            {
                double min = Math.min(x, y);
                double mean = ExtraMath.average(x, y);
                double max = Math.max(x, y);

                double meanReverse = ExtraMath.average(y, x);
                double meanNegative = ExtraMath.average(-y, -x);

                Assert.assertTrue(min + " <= " + mean, min <= mean);
                Assert.assertTrue(mean + " <= " + max, mean <= max);

                Assert.assertEquals(mean, meanReverse, 0.0);

                if (mean == 0.0)
                {
                    Assert.assertEquals(x, -y, 0.0);
                }
                if (x == -y)
                {
                    Assert.assertEquals(0.0, mean, 0.0);
                }

                Assert.assertEquals(-mean, meanNegative, 0.0);
            }
        }
    }

    @Test
    public void testMidpoint()
    {
        final double BASE = 360.0;
        final double DELTA = Math.ulp(BASE);
        final double[] NUMBERS = {
            0.0, 12.3,
            90.0, 101.01,
            180.0, 222.2,
            270.0, 333.3,
            };

        String message;

        for (double x : NUMBERS)
        {
            message = "x = " + x;

            Assert.assertEquals(
                message,
                x,
                ExtraMath.midpoint(BASE, x, x),
                0.0);

            double xp = ExtraMath.nextUp(BASE, x);
            double xpp = ExtraMath.nextUp(BASE, xp);
            double xm = ExtraMath.nextDown(BASE, x);
            double xmm = ExtraMath.nextDown(BASE, xm);

            double midXmXp = ExtraMath.midpoint(BASE, xm, xp);
            double midXmmXpp = ExtraMath.midpoint(BASE, xmm, xpp);
            double midXXpp = ExtraMath.midpoint(BASE, x, xpp);
            double midXmmX = ExtraMath.midpoint(BASE, xmm, x);

            Assert.assertEquals(message, x, midXmXp, 0.0);
            Assert.assertTrue(
                message + ": "
                    + midXmmXpp + " in {" + xm + ", " + x + "}",
                midXmmXpp == xm || midXmmXpp == x);
            Assert.assertEquals(message, xp, midXXpp, 0.0);
            Assert.assertEquals(message, xm, midXmmX, 0.0);

            assertEquals(
                message,
                BASE,
                ExtraMath.modulus(BASE, x + BASE/2.0),
                ExtraMath.midpoint(BASE, xp, xm),
                DELTA);
            assertEquals(
                message,
                BASE,
                ExtraMath.modulus(BASE, x + BASE/2.0),
                ExtraMath.midpoint(BASE, xpp, xmm),
                DELTA);
            assertEquals(
                message,
                BASE,
                ExtraMath.modulus(BASE, xp + BASE/2.0),
                ExtraMath.midpoint(BASE, xpp, x),
                DELTA);
            assertEquals(
                message,
                BASE,
                ExtraMath.modulus(BASE, xm + BASE/2.0),
                ExtraMath.midpoint(BASE, x, xmm),
                DELTA);

            for (double y : NUMBERS)
            {
                message = "x = " + x + ", y = " + y;

                double midpoint = ExtraMath.midpoint(BASE, x, y);

                if (x <= y)
                {
                    Assert.assertTrue(
                        message + ": " + x + " <= " + midpoint,
                        x <= midpoint);
                    Assert.assertTrue(
                        message + ": " + midpoint + " <= " + y,
                        midpoint <= y);
                }
                else
                {
                    Assert.assertTrue(
                        message + ": "
                            + midpoint + " < " + y + " or "
                            + x + " < " + midpoint,
                        midpoint <= y || x <= midpoint);
                }

                if (x != y)
                {
                    double midpointReverse =
                        ExtraMath.midpoint(BASE, y, x);

                    assertEquals(
                        message,
                        BASE,
                        ExtraMath.modulus(BASE, midpoint + BASE/2.0),
                        midpointReverse,
                        DELTA);
                }

                double midpointOpposite = ExtraMath.midpoint(
                    BASE,
                    ExtraMath.modulus(BASE, x + BASE/2.0),
                    ExtraMath.modulus(BASE, y + BASE/2.0));

                assertEquals(
                    message,
                    BASE,
                    ExtraMath.modulus(BASE, midpoint + BASE/2.0),
                    midpointOpposite,
                    DELTA);
            }
        }
    }
}
