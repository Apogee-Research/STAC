package com.stac.image.utilities;

/**
 *
 */
public class ARGB {
    public static float getA(int argb) {
        return ((float) ((argb >> 24) & 0xFF)) / 255;
    }

    public static int rawA(int argb) {
        return (argb >> 24) & 0xFF;
    }

    public static float getR(int argb) {
        return ((float) ((argb >> 16) & 0xFF)) / 255;
    }

    public static int rawR(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static float getG(int argb) {
        return ((float) ((argb >> 8) & 0xFF)) / 255;
    }

    public static int rawG(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static float getB(int argb) {
        return ((float) ((argb & 0xFF))) / 255;
    }

    public static int rawB(int argb) {
        return (argb & 0xFF);
    }

    public static int toARGB(float a, float r, float g, float b) {
        int A = ((int) a * 255) << 24;
        int R = ((int) r * 255) << 16;
        int G = ((int) g * 255) << 8;
        int B = ((int) b * 255);
        return A + R + G + B;
    }

    public static int toARGB(int a, int r, int g, int b) {
        int A = a << 24;
        int R = r << 16;
        int G = g << 8;
        int B = b;
        return A + R + G + B;
    }

    public static float[] toHSVA(int argb) {
        float[] hsva = new float[4];

        float b = ARGB.getB(argb);
        float g = ARGB.getG(argb);
        float r = ARGB.getR(argb);

        hsva[3] = ARGB.getA(argb);
        hsva[2] = Math.max(r, Math.max(g, b));

        float Cmin = Math.min(r, Math.min(g, b));
        float Cdelt = hsva[2] - Cmin;

        hsva[1] = (hsva[2] == 0) ? 0 : Cdelt / hsva[2];

        hsva[0] = 60f;
        if (Cdelt == 0) {
            hsva[0] = 0;
        } else if (hsva[2] == r) {
            hsva[0] *= ((g-b)/Cdelt) % 6f;
        } else if (hsva[2] == g) {
            hsva[0] *= ((b-r)/Cdelt) + 2f;
        } else {
            hsva[0] *= ((r-g)/Cdelt) + 4f;
        }

        return hsva;
    }
}
