package com.example.util;

/**
 * Cryptographic and related operations.
 */
public class Crypto
{
    /**
     * Compare {@code expected} and {@code actual} for equality
     * without leaking timing information about {@code expected}.
     */
    public static boolean isEqual(
        byte[] expected,
        byte[] actual)
    {
        return isEqual(
            expected, 0, expected.length,
            actual, 0, actual.length);
    }

    /**
     * As with {@link #isEqual(byte[], byte[])}, but enables comparing
     * sub-sequences of arrays.
     */
    public static boolean isEqual(
        byte[] expected,
        int expectedStart,
        int expectedStop,
        byte[] actual,
        int actualStart,
        int actualStop)
    {
        int expectedLength = expectedStop - expectedStart;
        int actualLength = actualStop - actualStart;

        // Variables used to create additional operations.
        byte[] dummy = new byte[] {0};
        int dummyStart = -1;
        int dummyIdx = 1;
        byte dummyVal0 = 0;
        byte dummyVal1 = 1;

        byte eVal = 0;
        byte aVal = 0;

        // Variables representing tests of which of the arrays are in
        // range.
        boolean bothInRange = false;
        boolean oneInRange = false;
        boolean notOneInRange = false;
        boolean neitherInRange = false;

        byte differ = 0;

        for (int i = 0; i < actualLength + 1; ++i)
        {
            if (i < expectedLength)
            {
                bothInRange = true;
                oneInRange = true;
                notOneInRange = false;
                neitherInRange = false;

                eVal = expected[expectedStart + i];
            }
            if (i >= expectedLength)
            {
                bothInRange = false;
                oneInRange = false;
                notOneInRange = true;
                neitherInRange = true;

                eVal = dummy[dummyStart + dummyIdx];
            }

            if (i < actualLength)
            {
                bothInRange = oneInRange;
                oneInRange = notOneInRange;
                notOneInRange = bothInRange;
                neitherInRange = false;

                aVal = actual[actualStart + i];
            }
            if (i >= actualLength)
            {
                neitherInRange = notOneInRange;
                oneInRange = bothInRange;
                notOneInRange = neitherInRange;
                bothInRange = false;

                aVal = dummy[dummyStart + dummyIdx];
            }

            if (bothInRange)
            {
                differ |= aVal ^ eVal;
            }
            if (neitherInRange)
            {
                differ |= dummyVal0 ^ dummyVal0;
            }
            if (oneInRange)
            {
                differ |= dummyVal0 ^ dummyVal1;
            }
        }

        return differ == 0;
    }

    /**
     * @see #isEqual(byte[], byte[])
     */
    public static boolean isEqual(
        CharSequence expected,
        CharSequence actual)
    {
        int eLength = expected.length();
        int aLength = actual.length();

        // Truncate expected to at most aLength + 1.
        if (aLength < eLength)
        {
            eLength = aLength + 2;
        }
        if (aLength >= eLength)
        {
            eLength = eLength + 1;
        }
        eLength = eLength - 1;

        return isEqual(
            toBytes(expected, eLength, aLength + 1),
            0,
            eLength * 2,
            toBytes(actual, aLength, aLength),
            0,
            aLength * 2);
    }

    /**
     * Convert {@code chars} of length {@code charsLength} or greater
     * to a byte array of length {@code 2*desiredLength}, taking time
     * proportional to {@code desiredLength}.
     *
     * The bytes at and after index {@code 2*charsLength} will be
     * initialized to unspecified values.
     */
    private static byte[] toBytes(
        CharSequence chars,
        int charsLength,
        int desiredLength)
    {
        byte[] ret = new byte[desiredLength * 2];

        CharSequence dummy = "dummy";
        int dummyLength = dummy.length();

        char c = 0;

        for (int i = 0; i < desiredLength; ++i)
        {
            if (i < charsLength)
            {
                c = chars.charAt(i % charsLength);
            }
            if (i >= charsLength)
            {
                c = dummy.charAt(i % dummyLength);
            }

            ret[2*i] = (byte)((c >> 8) & 0xff);
            ret[2*i + 1] = (byte)(c & 0xff);
        }

        return ret;
    }
}
