package com.stac.learning;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InvalidObjectException;
import java.util.Arrays;

/**
 *
 */
public class VectorTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();
    public final VectorFactory ctor = new EuclideanVectorFactory();

    @Test
    public void testVectorBuilder() throws Exception {
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder.add(.0f);
        vectorBuilder.add(.2f);
        vectorBuilder.add(.4f);
        vectorBuilder.add(.6f);
        vectorBuilder.add(.8f);
        vectorBuilder.add(1.f);
        Vector build = vectorBuilder.build();
        Assert.assertEquals("Expect that the vector is the correct size", 6, build.size());
        Assert.assertTrue("Expect that the vector contains the correct values", Arrays.equals(build.getAttributes(), new float[]{.0f, .2f, .4f, .6f, .8f, 1.f}));

        vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder
                .add(.0f)
                .add(.2f)
                .add(.4f)
                .add(.6f)
                .add(.8f)
                .add(1.f);
        build = vectorBuilder.build();
        Assert.assertEquals("Expect that the vector is the correct size", 6, build.size());
        Assert.assertTrue("Expect that the vector contains the correct values", Arrays.equals(build.getAttributes(), new float[]{.0f, .2f, .4f, .6f, .8f, 1.f}));
    }

    @Test
    public void testIllegalArgumentsOn10() throws Exception {
        expected.expect(IllegalArgumentException.class);
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder.add(10.f);
        Assert.fail("Exception was not thrown correctly");
    }

    @Test
    public void testIllegalArgumentsOnNeg1() throws Exception {
        expected.expect(IllegalArgumentException.class);
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder.add(-1.f);
        Assert.fail("Exception was not thrown correctly");
    }

    @Test
    public void testInvalidObjectOnBuildTwice() throws Exception {
        expected.expect(InvalidObjectException.class);
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ctor, 2);
        vectorBuilder.build();
        vectorBuilder.build();
        Assert.fail("Exception was not thrown correctly");
    }

    @Test
    public void testCompareTo() throws Exception {
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder.add(.0f);
        vectorBuilder.add(.2f);
        vectorBuilder.add(.4f);
        vectorBuilder.add(.6f);
        vectorBuilder.add(.8f);
        vectorBuilder.add(1.f);
        Vector one = vectorBuilder.build();

        vectorBuilder = new Vector.VectorBuilder(ctor, 6);
        vectorBuilder
                .add(.0f)
                .add(.2f)
                .add(.4f)
                .add(.6f)
                .add(.8f)
                .add(1.f);
        Vector two = vectorBuilder.build();

        double v = one.compareTo(two);

        Assert.assertEquals("These vectors are 100% likely to be the same.", 1.0, v, 0.0001);
    }

    @Test
    public void testBig() throws Exception {
        int size = 200000000;
        Vector.VectorBuilder vb1 = new Vector.VectorBuilder(ctor, size);
        Vector.VectorBuilder vb2 = new Vector.VectorBuilder(ctor, size);
        for (int i = 0; i < size; i++) {
            vb1.add((float) i / (float) size);
            vb2.add((float) (i + 1) / (float) (size + 1));
        }
        Vector v1 = vb1.build();
        Vector v2 = vb2.build();

        Assert.assertNotEquals("These vectors are not 100% likely to be the same.", 1.0, v1.compareTo(v2), 0.00001);
    }

    @Test
    public void testTiny() throws Exception {
        int size = 200000000;
        float[] arr1 = new float[size];
        float[] arr2 = new float[size];
        for (int i = 0; i < size; i++) {
            arr1[i] = ((float) i / (float) size);
            arr2[i] = ((float) (i + 1) / (float) (size + 1));
        }

        float sumDelta = 0;
        for (int i = 0; i < size; i++) {
            sumDelta += (arr2[i] - arr1[i]);
        }

        Assert.assertNotEquals("These vectors are not 100% likely to be the same.", 1.0, 1 - Math.sqrt(sumDelta), 0.0001);
    }
}