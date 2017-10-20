package com.stac.image.algorithms.generics;

import com.stac.image.utilities.ARGB;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

/**
 *
 */
public class CannyEdgeDetect {
    private static BufferedImage getSobelH(BufferedImage image) {
        return Convolve.convolve(image, SobelH);
    }

    private static BufferedImage getSobelV(BufferedImage image) {
        return Convolve.convolve(image, SobelV);
    }

    private static BufferedImage getAngles(BufferedImage Gx, BufferedImage Gy) {
        BufferedImage angles = new BufferedImage(Gx.getWidth(), Gx.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < Gx.getWidth(); i++) {
            for (int j = 0; j < Gx.getHeight(); j++) {
                int gx = ARGB.rawB(Gx.getRGB(i, j));
                int gy = ARGB.rawB(Gy.getRGB(i, j));
                int tanpi8gx = 27146, tan3pi8gx = 158218;

                if (gx != 0) {
                    if (gx < 0) {
                        gx = -gx;
                        gy = -gy;
                    }
                    gy <<= 16;
                    tanpi8gx *= gx;
                    tan3pi8gx *= gx;
                    if (gy > -tan3pi8gx && gy < -tanpi8gx) {
                        setExpandedValue(angles, i, j, Direction.UP45.ordinal());
                        continue;
                    }
                    if (gy > -tanpi8gx && gy < tanpi8gx) {
                        setExpandedValue(angles, i, j, Direction.HORIZONTAL.ordinal());
                        continue;
                    }
                    if (gy > tanpi8gx && gy < tan3pi8gx) {
                        setExpandedValue(angles, i, j, Direction.DOWN45.ordinal());
                        continue;
                    }
                }
                setExpandedValue(angles, i, j, Direction.VERTICAL.ordinal());
            }
        }
        return angles;
    }

    private static void setExpandedValue(BufferedImage image, int i, int j, int value) {
        image.setRGB(i, j, ARGB.toARGB(255, value, value, value));
    }


    private static BufferedImage getGradient(BufferedImage Gx, BufferedImage Gy) {
        BufferedImage gradient = new BufferedImage(Gx.getWidth(), Gx.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < Gx.getWidth(); i++) {
            for (int j = 0; j < Gx.getHeight(); j++) {
                int gx = ARGB.rawB(Gx.getRGB(i, j));
                int gy = ARGB.rawB(Gy.getRGB(i, j));
                setExpandedValue(gradient, i, j, (int) Math.sqrt(gy * gy + gx * gx));
            }
        }
        return gradient;
    }


    private static BufferedImage nonMaxSupression(BufferedImage angles, BufferedImage gradient) {
        BufferedImage nms = new BufferedImage(angles.getWidth(), angles.getHeight(), angles.getType());

        nms.setData(gradient.copyData(null));

        for (int i = 0; i < angles.getWidth(); i++) {
            for (int j = 0; j < angles.getHeight(); j++) {
                int[] magnitudes = getMags(i, j, Direction.getDirection(ARGB.rawB(angles.getRGB(i, j))), gradient);
                if (Math.max(magnitudes[1], Math.max(magnitudes[0], magnitudes[2])) != magnitudes[1]) {
                    setExpandedValue(nms, i, j, 0);
                }
            }
        }

        return nms;
    }

    private static BufferedImage hysteresisThresholding(BufferedImage input, int min, int max) {
        BufferedImage nms = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

        nms.setData(input.copyData(null));

        for (int i = 0; i < nms.getWidth(); i++) {
            for (int j = 0; j < nms.getHeight(); j++) {
                int m11 = ARGB.rawB(nms.getRGB(bound(0, nms.getWidth(), i), bound(0, nms.getHeight(), j)));

                if (m11 >= max) {
                    hysterize(min, nms, i-1, j-1);
                    hysterize(min, nms, i, j-1);
                    hysterize(min, nms, i+1, j-1);
                    hysterize(min, nms, i-1, j);
                    hysterize(min, nms, i, j);
                    hysterize(min, nms, i+1, j);
                    hysterize(min, nms, i-1, j+1);
                    hysterize(min, nms, i, j+1);
                    hysterize(min, nms, i+1, j+1);
                } else if (m11 < min) {
                    setExpandedValue(nms, i, j, 0);
                }
            }
        }
        return nms;
    }

    private static void hysterize(int min, BufferedImage nms, int i, int j) {
        i = bound(0, nms.getWidth(), i);
        j = bound(0, nms.getHeight(), j);
        if (ARGB.rawB(nms.getRGB(i, j)) > min) {
            setExpandedValue(nms, i, j, 255);
        }
    }

    private static int[] getMags(int i, int j, Direction angle, BufferedImage grad) {
        int bx = i, by = j, ax = i, ay = j;

        switch (angle) {
            case DOWN45:
                bx--;
                by--;
                ax++;
                ay++;
                break;
            case UP45:
                bx--;
                by++;
                ax++;
                ay--;
                break;
            case VERTICAL:
                by--;
                ay++;
                break;
            case HORIZONTAL:
                bx--;
                ax++;
                break;
        }
        bx = bound(0, grad.getWidth(), bx);
        by = bound(0, grad.getHeight(), by);
        ax = bound(0, grad.getWidth(), ax);
        ay = bound(0, grad.getHeight(), ay);

        return new int[]{
                ARGB.rawB(grad.getRGB(bx,by)),
                ARGB.rawB(grad.getRGB(i,j)),
                ARGB.rawB(grad.getRGB(ax,ay))
        };
    }

    private static int bound(int min, int max, int val) {
        if (val < min) val++;
        if (val >= max) val--;
        return val;
    }

    private static final Kernel SobelV = new Kernel(3, 3, new float[]{
            1f, 2f, 1f,
            0f, 0f, 0f,
            -1f, -2f, -1f
    });

    private static final Kernel SobelH = new Kernel(3, 3, new float[]{
            1f, 0f, -1f,
            2f, 0f, -2f,
            1f, 0f, -1f
    });

    public static BufferedImage detect(BufferedImage image, int min, int thresh) {
        BufferedImage blurred = Convolve.convolve(image, Convolve.Gausian5x5);
        BufferedImage grey = ConvertImage.otherGray(blurred);
        BufferedImage sobelH = CannyEdgeDetect.getSobelH(grey);
        BufferedImage sobelV = CannyEdgeDetect.getSobelV(grey);
        BufferedImage angle = CannyEdgeDetect.getAngles(sobelH, sobelV);
        BufferedImage grad = CannyEdgeDetect.getGradient(sobelH, sobelV);
        BufferedImage nms = CannyEdgeDetect.nonMaxSupression(angle, grad);
        BufferedImage output = CannyEdgeDetect.hysteresisThresholding(nms, min, thresh);

        return output;
    }
}
