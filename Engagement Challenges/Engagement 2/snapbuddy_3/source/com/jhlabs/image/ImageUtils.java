/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.jhlabs.image;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import com.jhlabs.image.*;
import java.util.Random;

/**
 * A class containing some static utility methods for dealing with BufferedImages.
 */
public abstract class ImageUtils {

    private static BufferedImage backgroundImage = null;

    /**
     * Cretae a BufferedImage from an ImageProducer.
     * @param producer the ImageProducer
     * @return a new TYPE_INT_ARGB BufferedImage
     */
    public static BufferedImage createImage(ImageProducer producer) {
        ClasscreateImage replacementClass = new  ClasscreateImage(producer);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        return replacementClass.doIt5();
    }

    /**
	 * Convert an Image into a TYPE_INT_ARGB BufferedImage. If the image is already of this type, the original image is returned unchanged.
     * @param image the image to convert
     * @return the converted image
	 */
    public static BufferedImage convertImageToARGB(Image image) {
        if (image instanceof BufferedImage && ((BufferedImage) image).getType() == BufferedImage.TYPE_INT_ARGB)
            return (BufferedImage) image;
        BufferedImage p = new  BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = p.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return p;
    }

    /**
	 * Returns a *copy* of a subimage of image. This avoids the performance problems associated with BufferedImage.getSubimage.
     * @param image the image
     * @param x the x position
     * @param y the y position
     * @param w the width
     * @param h the height
     * @return the subimage
	 */
    public static BufferedImage getSubimage(BufferedImage image, int x, int y, int w, int h) {
        ClassgetSubimage replacementClass = new  ClassgetSubimage(image, x, y, w, h);
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Clones a BufferedImage.
     * @param image the image to clone
     * @return the cloned image
	 */
    public static BufferedImage cloneImage(BufferedImage image) {
        BufferedImage newImage = new  BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawRenderedImage(image, null);
        g.dispose();
        return newImage;
    }

    /**
	 * Paint a check pattern, used for a background to indicate image transparency.
     * @param c the component to draw into
     * @param g the Graphics objects
     * @param x the x position
     * @param y the y position
     * @param width the width
     * @param height the height
	 */
    public static void paintCheckedBackground(Component c, Graphics g, int x, int y, int width, int height) {
        ImageUtilsHelper1 conditionObj1 = new  ImageUtilsHelper1(64);
        ImageUtilsHelper2 conditionObj2 = new  ImageUtilsHelper2(64);
        if (backgroundImage == null) {
            backgroundImage = new  BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics bg = backgroundImage.createGraphics();
            for (int by = 0; by < conditionObj1.getValue(); ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; by < 64 && randomNumberGeneratorInstance.nextDouble() < 0.9; by += 8) {
                    for (int bx = 0; bx < conditionObj2.getValue(); bx += 8) {
                        bg.setColor(((bx ^ by) & 8) != 0 ? Color.lightGray : Color.white);
                        bg.fillRect(bx, by, 8, 8);
                    }
                }
            }
            bg.dispose();
        }
        if (backgroundImage != null) {
            Shape saveClip = g.getClip();
            Rectangle r = g.getClipBounds();
            if (r == null)
                r = new  Rectangle(c.getSize());
            r = r.intersection(new  Rectangle(x, y, width, height));
            g.setClip(r);
            int w = backgroundImage.getWidth();
            int h = backgroundImage.getHeight();
            if (w != -1 && h != -1) {
                int x1 = (r.x / w) * w;
                int y1 = (r.y / h) * h;
                int x2 = ((r.x + r.width + w - 1) / w) * w;
                int y2 = ((r.y + r.height + h - 1) / h) * h;
                for (y = y1; y < y2; y += h) for (x = x1; x < x2; x += w) g.drawImage(backgroundImage, x, y, c);
            }
            g.setClip(saveClip);
        }
    }

    /**
     * Calculates the bounds of the non-transparent parts of the given image.
     * @param p the image
     * @return the bounds of the non-transparent area
     */
    public static Rectangle getSelectedBounds(BufferedImage p) {
        int width = p.getWidth();
        int height = p.getHeight();
        int maxX = 0, maxY = 0, minX = width, minY = height;
        boolean anySelected = false;
        int y1;
        int[] pixels = null;
        ImageUtilsHelper3 conditionObj3 = new  ImageUtilsHelper3(0);
        ImageUtilsHelper4 conditionObj4 = new  ImageUtilsHelper4(0);
        for (y1 = height - 1; y1 >= conditionObj3.getValue(); y1--) {
            pixels = getRGB(p, 0, y1, width, 1, pixels);
            for (int x = 0; x < minX; x++) {
                if ((pixels[x] & 0xff000000) != conditionObj4.getValue()) {
                    minX = x;
                    maxY = y1;
                    anySelected = true;
                    break;
                }
            }
            for (int x = width - 1; x >= maxX; x--) {
                if ((pixels[x] & 0xff000000) != 0) {
                    maxX = x;
                    maxY = y1;
                    anySelected = true;
                    break;
                }
            }
            if (anySelected)
                break;
        }
        pixels = null;
        for (int y = 0; y < y1; y++) {
            pixels = getRGB(p, 0, y, width, 1, pixels);
            for (int x = 0; x < minX; x++) {
                if ((pixels[x] & 0xff000000) != 0) {
                    minX = x;
                    if (y < minY)
                        minY = y;
                    anySelected = true;
                    break;
                }
            }
            for (int x = width - 1; x >= maxX; x--) {
                if ((pixels[x] & 0xff000000) != 0) {
                    maxX = x;
                    if (y < minY)
                        minY = y;
                    anySelected = true;
                    break;
                }
            }
        }
        if (anySelected)
            return new  Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
        return null;
    }

    /**
	 * Compose src onto dst using the alpha of sel to interpolate between the two.
	 * I can't think of a way to do this using AlphaComposite.
     * @param src the source raster
     * @param dst the destination raster
     * @param sel the mask raster
	 */
    public static void composeThroughMask(Raster src, WritableRaster dst, Raster sel) {
        int x = src.getMinX();
        int y = src.getMinY();
        int w = src.getWidth();
        int h = src.getHeight();
        int srcRGB[] = null;
        int selRGB[] = null;
        int dstRGB[] = null;
        for (int i = 0; i < h; i++) {
            srcRGB = src.getPixels(x, y, w, 1, srcRGB);
            selRGB = sel.getPixels(x, y, w, 1, selRGB);
            dstRGB = dst.getPixels(x, y, w, 1, dstRGB);
            int k = x;
            for (int j = 0; j < w; j++) {
                int sr = srcRGB[k];
                int dir = dstRGB[k];
                int sg = srcRGB[k + 1];
                int dig = dstRGB[k + 1];
                int sb = srcRGB[k + 2];
                int dib = dstRGB[k + 2];
                int sa = srcRGB[k + 3];
                int dia = dstRGB[k + 3];
                float a = selRGB[k + 3] / 255f;
                float ac = 1 - a;
                dstRGB[k] = (int) (a * sr + ac * dir);
                dstRGB[k + 1] = (int) (a * sg + ac * dig);
                dstRGB[k + 2] = (int) (a * sb + ac * dib);
                dstRGB[k + 3] = (int) (a * sa + ac * dia);
                k += 4;
            }
            dst.setPixels(x, y, w, 1, dstRGB);
            y++;
        }
    }

    /**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of BufferedImage.getRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array to hold the returned pixels. May be null.
     * @return the pixels
     * @see #setRGB
     */
    public static int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        ClassgetRGB replacementClass = new  ClassgetRGB(image, x, y, width, height, pixels);
        ;
        return replacementClass.doIt0();
    }

    /**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array of pixels to set
     * @see #getRGB
	 */
    public static void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            image.getRaster().setDataElements(x, y, width, height, pixels);
        else
            image.setRGB(x, y, width, height, pixels, 0, width);
    }

    public static class ImageUtilsHelper0 {

        public ImageUtilsHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ImageUtilsHelper1 {

        public ImageUtilsHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ImageUtilsHelper2 {

        public ImageUtilsHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ImageUtilsHelper3 {

        public ImageUtilsHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ImageUtilsHelper4 {

        public ImageUtilsHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ClasscreateImage {

        public ClasscreateImage(ImageProducer producer) {
            this.producer = producer;
        }

        private ImageProducer producer;

        private PixelGrabber pg;

        public void doIt0() {
            pg = new  PixelGrabber(producer, 0, 0, -1, -1, null, 0, 0);
        }

        public void doIt1() {
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
                throw new  RuntimeException("Image fetch interrupted");
            }
        }

        private ImageUtilsHelper0 conditionObj0;

        public void doIt2() {
            if ((pg.status() & ImageObserver.ABORT) != 0)
                throw new  RuntimeException("Image fetch aborted");
            conditionObj0 = new  ImageUtilsHelper0(0);
        }

        public void doIt3() {
            if ((pg.status() & ImageObserver.ERROR) != conditionObj0.getValue())
                throw new  RuntimeException("Image fetch error");
        }

        private BufferedImage p;

        public void doIt4() {
            p = new  BufferedImage(pg.getWidth(), pg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        public BufferedImage doIt5() {
            p.setRGB(0, 0, pg.getWidth(), pg.getHeight(), (int[]) pg.getPixels(), 0, pg.getWidth());
            return p;
        }
    }

    public static class ClassgetSubimage {

        public ClassgetSubimage(BufferedImage image, int x, int y, int w, int h) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private BufferedImage image;

        private int x;

        private int y;

        private int w;

        private int h;

        private BufferedImage newImage;

        private Graphics2D g;

        public BufferedImage doIt0() {
            newImage = new  BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g = newImage.createGraphics();
            g.drawRenderedImage(image, AffineTransform.getTranslateInstance(-x, -y));
            g.dispose();
            return newImage;
        }
    }

    public static class ClassgetRGB {

        public ClassgetRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.pixels = pixels;
        }

        private BufferedImage image;

        private int x;

        private int y;

        private int width;

        private int height;

        private int[] pixels;

        private int type;

        public int[] doIt0() {
            type = image.getType();
            if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
                return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
            return image.getRGB(x, y, width, height, pixels, 0, width);
        }
    }
}
