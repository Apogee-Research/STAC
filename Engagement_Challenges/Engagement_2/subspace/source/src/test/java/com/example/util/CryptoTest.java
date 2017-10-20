package com.example.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class CryptoTest
{
    @RunWith(Parameterized.class)
    public static class IsEqualTest
    {
        @Parameterized.Parameters(
            name = "{index}: isEqual({0}, {1}) = {2}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] {
                {"", "", true},
                {"abc", "abc", true},
                {"abcdefhij", "abcdefhij", true},
                {"abc", "def", false},
                {"ab", "abc", false},
                });
        }

        @Parameterized.Parameter(value = 0)
        public String x;

        @Parameterized.Parameter(value = 1)
        public String y;

        @Parameterized.Parameter(value = 2)
        public boolean equals;

        @Test
        public void test()
        {
            if (equals)
            {
                Assert.assertTrue(Crypto.isEqual(x, y));
                Assert.assertTrue(Crypto.isEqual(y, x));
            }
            else
            {
                Assert.assertFalse(Crypto.isEqual(x, y));
                Assert.assertFalse(Crypto.isEqual(y, x));
            }
        }
    }
}
